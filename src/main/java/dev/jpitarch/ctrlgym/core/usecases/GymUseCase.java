package dev.jpitarch.ctrlgym.core.usecases;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.domain.exceptions.CoreBusinessException;

import java.time.LocalDate;
import java.time.YearMonth;

import dev.jpitarch.ctrlgym.core.domain.exceptions.ExerciseNotFoundException;
import dev.jpitarch.ctrlgym.core.domain.exceptions.ProductNotFoundException;
import dev.jpitarch.ctrlgym.core.dto.CreateEmployeeRequest;
import dev.jpitarch.ctrlgym.core.dto.CreateOrderRequest;
import dev.jpitarch.ctrlgym.core.dto.CurrentOccupancy;
import dev.jpitarch.ctrlgym.core.dto.GymScheduleResponse;
import dev.jpitarch.ctrlgym.core.dto.LegalDocumentResponse;
import dev.jpitarch.ctrlgym.core.dto.MemberMetrics;
import dev.jpitarch.ctrlgym.core.dto.MemberRetention;
import dev.jpitarch.ctrlgym.core.dto.TimeRange;
import dev.jpitarch.ctrlgym.core.entities.GymScheduleEntity;
import dev.jpitarch.ctrlgym.core.entities.PostalCodeEntity;
import dev.jpitarch.ctrlgym.core.events.EmployeeCreatedEvent;
import dev.jpitarch.ctrlgym.core.events.OrderCreatedEvent;
import dev.jpitarch.ctrlgym.core.repositories.EmployeesRepository;
import dev.jpitarch.ctrlgym.core.repositories.AnalyticsRepository;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.InvoiceRepository;
import dev.jpitarch.ctrlgym.core.repositories.LegalDocumentsRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembershipPlanRepository;
import dev.jpitarch.ctrlgym.core.repositories.OrderRepository;
import dev.jpitarch.ctrlgym.core.repositories.ProductRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.GymScheduleJpaRepository;
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
import dev.jpitarch.ctrlgym.storage.config.StorageBucket;
import dev.jpitarch.ctrlgym.storage.services.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
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

  private final ApplicationEventPublisher eventPublisher;

  private final LegalDocumentsRepository legalDocumentsRepository;

  private final OrderRepository orderRepository;

  private final GymScheduleJpaRepository gymScheduleJpaRepository;

  private final AnalyticsRepository analyticsRepository;

  public GymScheduleResponse getSchedule(Integer gymId) {
    Map<Integer, TimeRange> schedule = gymScheduleJpaRepository.findByGymId(gymId).stream()
      .collect(java.util.stream.Collectors.toMap(
        GymScheduleEntity::getDayOfWeek,
        entity -> TimeRange.of(entity.getOpensAt(), entity.getClosesAt())
      ));
    return new GymScheduleResponse(schedule);
  }


  public List<GymBranch> getBranches(Integer gymId) {
    return gymsRepository.getBranches(gymId);
  }

  public List<String> getBranchImages(Integer gymId, Integer branchId) {
    String prefix = "tenants/" + gymId + "/branches/" + branchId + "/";
    return storageService.listObjectsByPrefix(prefix, StorageBucket.ASSETS).stream()
      .map(storageService::resolvePublicUrl)
      .toList();
  }

  public List<Member> getMembers(GymBranchId gymBranchId, String q) {
    List<Member> users = gymsRepository.getMembers(gymBranchId, q);
    List<Integer> postalCodes = users.stream()
      .filter(m -> m.getAddress() != null && m.getAddress().getPostalCode() != null)
      .map(m -> m.getAddress().getPostalCode())
      .distinct()
      .toList();
    if (!postalCodes.isEmpty()) {
      Map<Integer, PostalCodeEntity> postalCodeMap = postalCodeJpaRepository.findMapByPostalCodeIn(postalCodes);
      users.forEach(m -> {
        if (m.getAddress() != null && m.getAddress().getPostalCode() != null) {
          PostalCodeEntity pc = postalCodeMap.get(m.getAddress().getPostalCode());
          if (pc != null) {
            m.getAddress().setCity(pc.getCity());
          }
        }
      });
    }
    users.forEach(this::resolveAvatarUrl);
    return users;
  }

  public MemberRetention getMemberRetention(GymBranchId gymBranchId, UUID memberId) {
    return gymsRepository.getMemberRetention(memberId);
  }

  public List<Invoice> getInvoices(GymBranchId gymBranchId, UUID memberId, Integer year) {
    return invoiceRepository.findByMemberIdAndYear(memberId, year);
  }


  public MembershipPlan createMembershipPlan(Integer gymId, MembershipPlan plan) throws StripeException {
    if ((plan.getGymBranchId() == null && !plan.isAllBranches()) || (plan.getGymBranchId() != null && plan.isAllBranches())) {
      throw new CoreBusinessException(MembershipPlan.class, "gymBranchId is informed and allBranches is true or vice versa");
    }

    boolean fromPresent = plan.getStartTime() != null;
    boolean toPresent = plan.getEndTime() != null;

    if (fromPresent != toPresent) {
      throw new CoreBusinessException(MembershipPlan.class, "Both 'start_time' and 'end_time' must be informed together or both null");
    }

    if (fromPresent && plan.isAllDay()) {
      throw new CoreBusinessException(MembershipPlan.class, "When 'start_time' and 'end_time' are informed, 'all_day' must be false or null");
    }

    String[] data = productService.create(gymId, plan);
    plan.setId(data[0]);
    membershipPlanRepository.create(plan, gymId, data[1]);
    return plan;
  }

  public MembershipPlan getMembershipPlan(String planId) {
    return membershipPlanRepository.retrieve(planId);
  }

  public List<MembershipPlan> getMembershipPlans(GymBranchId gymBranchId) {
    log.debug("Retrieving membership plans for gym with id {}...", gymBranchId);
    return membershipPlanRepository.getMembershipPlans(gymBranchId);
  }

  public void deleteMembershipPlan(String planId, Integer gymId) throws StripeException {
    productService.delete(gymId, planId);
    membershipPlanRepository.delete(planId);
  }

  public CurrentOccupancy getCurrentOccupancy(GymBranchId gymBranchId) {
    GymBranch gymBranch = gymsRepository.getGymBranch(gymBranchId);
    return new CurrentOccupancy(gymsRepository.getCurrentOccupancy(gymBranchId), gymBranch.getCapacity(), gymBranch.getPeakHour());
  }

  public List<Exercise> getAll(Integer gymId) {
    List<Exercise> exercises = exercisesService.getAll(gymId);
    exercises.forEach(this::resolveExerciseImageUrl);
    return exercises;
  }

  public Exercise createExercise(Integer gymId, Exercise exercise, MultipartFile image) {
    if (image != null && !image.isEmpty()) {
      String imageKey = storageService.uploadFile(image, gymId, "exercises", StorageBucket.ASSETS);
      exercise.setImage(imageKey);
    }
    Exercise created = exercisesService.create(exercise, gymId);
    resolveExerciseImageUrl(created);
    return created;
  }

  public void deleteExercise(Integer exerciseId, Integer gymId) {
    Exercise exercise = exercisesService.findById(exerciseId).orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

    if (exercise.getImage() != null && !exercise.getImage().isBlank()) {
      storageService.deleteFile(exercise.getImage(), StorageBucket.ASSETS);
    }
    exercisesService.delete(exerciseId, gymId);
  }

  public byte[] getMemberInvoiceReport(GymBranchId gymBranchId, UUID memberId, String invoiceId) throws IOException {
    log.info("Generating invoice report for member {} and invoice {}...", memberId, invoiceId);
    return generateInvoiceReportService.generate(memberId, invoiceId);
  }

  public Routine createGymRoutine(Integer gymId, Routine routine) {
    return routinesService.createForGym(routine, gymId);
  }

  public List<Routine> getGymRoutines(Integer gymId) {
    return routinesService.getGymRoutines(gymId);
  }

  public void deleteGymRoutine(Integer routineId, Integer gymId) {
    routinesService.deleteForGym(routineId, gymId);
  }

  public byte[] generateExpensesExcel(Integer gymId) throws IOException {
    return expensesService.generateExpensesExcel(gymId);
  }

  public List<ExpenseCategory> getExpenseCategories(Integer gymId) {
    return expensesService.getCategories(gymId);
  }

  public Expense createExpense(Expense expense, GymBranchId gymBranchId) {
    return expensesService.createExpense(expense, gymBranchId.branchId());
  }

  public void deleteExpense(Long expenseId) {
    expensesService.deleteExpense(expenseId);
  }

  public ExpenseCategory createExpenseCategory(Integer gymId, ExpenseCategory category) {
    return expensesService.createCategory(gymId, category);
  }

  public void deleteExpenseCategory(Integer categoryId, Integer gymId) {
    expensesService.deleteCategory(categoryId, gymId);
  }

  public ExpenseCategory updateExpenseCategoryName(Integer categoryId, Integer gymId, String newName) {
    return expensesService.updateCategoryName(categoryId, gymId, newName);
  }

  public List<Employee> getEmployees(GymBranchId gymBranchId) {
    return employeesRepository.getEmployees(gymBranchId);
  }

  @Transactional
  public void createEmployee(Integer gymId, CreateEmployeeRequest request) {
    if ((request.gymBranchId() == null && !request.allBranches()) ||
      (request.gymBranchId() != null && request.allBranches())) {
      throw new CoreBusinessException(Employee.class, "gymBranchId is informed and allBranches is true or vice versa");
    }

    String genderCode = request.gender() != null ? switch (request.gender()) {
      case MALE -> "M";
      case FEMALE -> "F";
    } : null;

    var user = employeesRepository.createEmployee(
      request.email(),
      gymId,
      request.name(),
      request.firstSurname(),
      request.secondSurname(),
      genderCode
    );

    var employeeId = user.getId();

    if (request.allBranches()) {
      employeesRepository.assignToAllBranches(employeeId, gymId);
    } else {
      employeesRepository.assignToBranch(employeeId, gymId, request.gymBranchId());
    }

    eventPublisher.publishEvent(new EmployeeCreatedEvent(this, user.getId(), gymId, request.email()));
  }

  public Product createProduct(Integer gymId, Integer branchId, Product product, MultipartFile image) {
    if (image != null && !image.isEmpty()) {
      String imageKey = storageService.uploadFile(image, gymId, "products", StorageBucket.ASSETS);
      product.setImage(imageKey);
    }
    Product created = productRepository.create(product, gymId, branchId);
    resolveProductImageUrl(created);
    return created;
  }

  public List<Product> getProducts(GymBranchId gymBranchId) {
    List<Product> products = productRepository.findByBranchId(gymBranchId.branchId());
    products.forEach(this::resolveProductImageUrl);
    return products;
  }

  public void deleteProduct(Integer productId) {
    Product product = productRepository.findById(productId)
      .orElseThrow(() -> new ProductNotFoundException(productId));
    if (product.getImage() != null && !product.getImage().isBlank()) {
      storageService.deleteFile(product.getImage(), StorageBucket.ASSETS);
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

  public List<LegalDocumentResponse> getActiveLegalDocuments(Integer gymId) {
    return legalDocumentsRepository.findAllActiveByGymId(gymId)
      .stream()
      .map(doc -> new LegalDocumentResponse(
        doc.getId(),
        doc.getType(),
        doc.getVersion(),
        doc.getContent(),
        doc.getEffectiveDate()
      ))
      .toList();
  }

  @Transactional
  public Order createOrder(GymBranchId gymBranchId, CreateOrderRequest request) {
    var order = Order.builder()
      .memberId(request.getMemberId())
      .gymId(gymBranchId.gymId())
      .gymBranchId(gymBranchId.branchId())
      .items(request.getItems().stream().map(item -> {
        Product product = productRepository.findById(item.getProductId())
          .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
        return Order.Item.builder()
          .productId(product.getId())
          .productName(product.getName())
          .productPrice(product.getPrice())
          .quantity(item.getQuantity())
          .build();
      }).toList())
      .build();

    Order savedOrder = orderRepository.create(order);

    eventPublisher.publishEvent(new OrderCreatedEvent(
      this,
      savedOrder.getId(),
      savedOrder.getMemberId(),
      savedOrder.getGymBranchId(),
      gymBranchId.gymId(),
      savedOrder.getItems().size()
    ));

    return savedOrder;
  }

  public List<Order> getOrders(GymBranchId gymBranchId) {
    return orderRepository.findByBranchId(gymBranchId.branchId());
  }

  public List<MemberMetrics> getMemberMetrics(UUID memberId, YearMonth from, YearMonth to) {
    return analyticsRepository.getMemberMetrics(memberId, from, to);
  }

  private void resolveAvatarUrl(Member member) {
    if (member.getAvatarUrl() != null) {
      String presignedUrl = storageService.generatePresignedUrl(member.getAvatarUrl().toString(), StorageBucket.AVATARS);
      member.setAvatarUrl(URI.create(presignedUrl));
    }
  }

  private void resolveExerciseImageUrl(Exercise exercise) {
    if (exercise.getImage() != null && !exercise.getImage().isBlank()) {
      exercise.setImage(storageService.resolvePublicUrl(exercise.getImage()));
    }
  }

  private void resolveProductImageUrl(Product product) {
    if (product.getImage() != null && !product.getImage().isBlank()) {
      product.setImage(storageService.resolvePublicUrl(product.getImage()));
    }
  }

}
