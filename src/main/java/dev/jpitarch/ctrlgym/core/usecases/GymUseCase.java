package dev.jpitarch.ctrlgym.core.usecases;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.domain.exceptions.CoreBusinessException;
import java.time.LocalDate;
import dev.jpitarch.ctrlgym.core.domain.exceptions.ExerciseNotFoundException;
import dev.jpitarch.ctrlgym.core.domain.exceptions.ProductNotFoundException;
import dev.jpitarch.ctrlgym.core.dto.CurrentOccupancy;
import dev.jpitarch.ctrlgym.core.dto.MemberRetention;
import dev.jpitarch.ctrlgym.core.models.PostalCodeMO;
import dev.jpitarch.ctrlgym.core.repositories.EmployeesRepository;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.InvoiceRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembershipPlanRepository;
import dev.jpitarch.ctrlgym.core.repositories.ProductRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.PostalCodeJpaRepository;
import dev.jpitarch.ctrlgym.core.dto.CreateShiftRequest;
import dev.jpitarch.ctrlgym.core.dto.CreateShiftSeriesRequest;
import dev.jpitarch.ctrlgym.core.dto.UpdateShiftRequest;
import dev.jpitarch.ctrlgym.core.services.EmployeeScheduleService;
import dev.jpitarch.ctrlgym.core.services.ExercisesService;
import dev.jpitarch.ctrlgym.core.services.ExpensesService;
import dev.jpitarch.ctrlgym.core.services.GenerateInvoiceReportService;
import dev.jpitarch.ctrlgym.core.services.RoutinesService;
import dev.jpitarch.ctrlgym.payments.services.ProductService;
import dev.jpitarch.ctrlgym.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GymUseCase {

  private final GymsRepository gymsRepository;

  private final ExercisesService exercisesService;

  private final MembershipPlanRepository membershipPlanRepository;

  private final InvoiceRepository invoiceRepository;

  private final ProductService productService;

  private final GenerateInvoiceReportService generateInvoiceReportService;

  private final RoutinesService routinesService;

  private final ExpensesService expensesService;

  private final PostalCodeJpaRepository postalCodeJpaRepository;

  private final StorageService storageService;

  private final EmployeesRepository employeesRepository;

  private final ProductRepository productRepository;

  private final EmployeeScheduleService employeeScheduleService;

  public List<GymBranch> getBranches(Integer gymId) {
    return gymsRepository.getBranches(gymId);
  }

  public List<Member> getMembers(GymBranchId gymBranchId, String q) {
    List<Member> users = gymsRepository.getMembers(gymBranchId, q);
    List<Integer> postalCodes = users.stream()
      .filter(m -> m.getAddress() != null && m.getAddress().getPostalCode() != null)
      .map(m -> m.getAddress().getPostalCode())
      .distinct()
      .toList();
    if (!postalCodes.isEmpty()) {
      Map<Integer, PostalCodeMO> postalCodeMap = postalCodeJpaRepository.findMapByPostalCodeIn(postalCodes);
      users.forEach(m -> {
        if (m.getAddress() != null && m.getAddress().getPostalCode() != null) {
          PostalCodeMO pc = postalCodeMap.get(m.getAddress().getPostalCode());
          if (pc != null) {
            m.getAddress().setCity(pc.getCity());
          }
        }
      });
    }
    return users;
  }

  public MemberRetention getMemberRetention(GymBranchId gymBranchId, Member.Id memberId) {
    return new MemberRetention(memberId, 85, 2340, 14, 9);
  }

  public Page<Invoice> getInvoices(GymBranchId gymBranchId, Member.Id memberId, Pageable pageable) {
    return invoiceRepository.findByMemberId(memberId, pageable);
  }


  public void createMembershipPlan(Integer gymId, MembershipPlan plan) throws StripeException {
    if ((plan.getGymBranchId() == null && !plan.isAllBranches()) || (plan.getGymBranchId() != null && plan.isAllBranches())) {
      throw new CoreBusinessException(MembershipPlan.class, "gymBranchId is informed and allBranches is true or vice versa");
    }
    String[] data = productService.create(gymId, plan);
    plan.setId(data[0]);
    membershipPlanRepository.create(plan, gymId, data[1]);
  }

  public List<MembershipPlan> getMembershipPlans(GymBranchId gymBranchId) {
    log.debug("Retrieving membership plans for gym with id {}...", gymBranchId);
    return membershipPlanRepository.getMembershipPlans(gymBranchId);
  }

  public void deleteMembershipPlan(String planId, Integer gymId) throws StripeException {
    productService.delete(gymId, planId);
    membershipPlanRepository.delete(planId, gymId);
  }

  public CurrentOccupancy getCurrentOccupancy(GymBranchId gymBranchId) {
    GymBranch gymBranch = gymsRepository.getGymBranch(gymBranchId);
    return new CurrentOccupancy(gymsRepository.getCurrentOccupancy(gymBranchId), gymBranch.getCapacity(), gymBranch.getPeakHour());
  }

  public List<Exercise> getAll(Integer gymId) {
    return exercisesService.getAll(gymId);
  }

  public Exercise createExercise(Integer gymId, Exercise exercise, MultipartFile image) {
    if (image != null && !image.isEmpty()) {
      String imageUrl = storageService.uploadFile(image, "exercises");
      exercise.setImage(imageUrl);
    }
    return exercisesService.create(exercise, gymId);
  }

  public void deleteExercise(Integer exerciseId, Integer gymId) {
    Exercise exercise = exercisesService.findById(exerciseId).orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

    if (exercise.getImage() != null && !exercise.getImage().isBlank()) {
      storageService.deleteFile(exercise.getImage());
    }
    exercisesService.delete(exerciseId, gymId);
  }

  public byte[] getMemberInvoiceReport(GymBranchId gymBranchId, Member.Id memberId, String invoiceId) throws IOException {
    log.info("Generating invoice report for member {} and invoice {}...", memberId, invoiceId);
    return generateInvoiceReportService.generate(memberId, invoiceId);
  }

  public void createGymRoutine(Integer gymId, Routine routine) {
    routinesService.createForGym(routine, gymId);
  }

  public List<Routine> getGymRoutines(Integer gymId) {
    return routinesService.getGymRoutines(gymId);
  }

  public void deleteGymRoutine(Integer routineId, Integer gymId) {
    routinesService.deleteForGym(routineId, gymId);
  }

  public byte[] generateExpensesExcel() throws IOException {
    return expensesService.generateExpensesExcel();
  }

  public List<Employee> getEmployees(GymBranchId gymBranchId) {
    return employeesRepository.getEmployees(gymBranchId);
  }

  public Product createProduct(Integer gymId, Integer branchId, Product product, MultipartFile image) {
    if (image != null && !image.isEmpty()) {
      String imageUrl = storageService.uploadFile(image, "products");
      product.setImage(imageUrl);
    }
    return productRepository.create(product, gymId, branchId);
  }

  public List<Product> getProducts(GymBranchId gymBranchId) {
    return productRepository.findByBranchId(gymBranchId.branchId());
  }

  public void deleteProduct(Integer productId) {
    Product product = productRepository.findById(productId)
      .orElseThrow(() -> new ProductNotFoundException(productId));
    if (product.getImage() != null && !product.getImage().isBlank()) {
      storageService.deleteFile(product.getImage());
    }
    productRepository.delete(productId);
  }

  public ShiftSeries createShiftSeries(Integer gymId, Integer gymBranchId, CreateShiftSeriesRequest request) {
    return employeeScheduleService.createSeries(gymId, gymBranchId, request);
  }

  public Shift createShift(Integer gymId, Integer gymBranchId, CreateShiftRequest request) {
    return employeeScheduleService.createSingleShift(gymId, gymBranchId, request);
  }

  public List<Shift> getShifts(UUID employeeId, Integer gymId, Integer gymBranchId, LocalDate from, LocalDate to) {
    if (from != null && to != null) {
      return employeeScheduleService.getShiftsByDateRange(employeeId, gymId, gymBranchId, from, to);
    }
    return employeeScheduleService.getAllShifts(employeeId, gymId, gymBranchId);
  }

  public List<ShiftSeries> getShiftSeries(UUID employeeId, Integer gymId, Integer gymBranchId) {
    return employeeScheduleService.getAllSeries(employeeId, gymId, gymBranchId);
  }

  public void deleteShiftSeries(Long seriesId) {
    employeeScheduleService.deleteSeries(seriesId);
  }

  public void deleteShift(Long shiftId) {
    employeeScheduleService.deleteShift(shiftId);
  }

  public Shift updateShift(Long shiftId, UpdateShiftRequest request) {
    return employeeScheduleService.updateShift(shiftId, request);
  }

}
