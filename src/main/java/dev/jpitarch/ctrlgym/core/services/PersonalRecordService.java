package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.Workout;
import dev.jpitarch.ctrlgym.core.dto.PersonalRecordResult;
import dev.jpitarch.ctrlgym.core.dto.PersonalRecordResult.RecordType;
import dev.jpitarch.ctrlgym.core.entities.MemberPersonalRecordEntity;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberPersonalRecordJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonalRecordService {

  private final MemberPersonalRecordJpaRepository jpaRepository;

  public List<PersonalRecordResult> calculate(UUID memberId, Workout workout) {
    List<PersonalRecordResult> results = new ArrayList<>();

    for (Workout.Exercise exercise : workout.getExercises()) {
      BestSet bestSet = findBestSet(exercise.getSets());
      if (bestSet == null) continue;

      List<MemberPersonalRecordEntity> existingRecords = jpaRepository
        .findByMemberIdAndExerciseIdOrderByAchievedAtDesc(memberId, exercise.getId());

      MemberPersonalRecordEntity currentRecord = existingRecords.isEmpty() ? null : existingRecords.getFirst();

      RecordType recordType = getRecordType(currentRecord, bestSet);
      if (recordType != null) {
        var newRecord = new MemberPersonalRecordEntity();
        newRecord.setMemberId(memberId);
        newRecord.setExerciseId(exercise.getId());
        newRecord.setWeight(bestSet.weight);
        newRecord.setReps(bestSet.reps);
        newRecord.setAchievedAt(OffsetDateTime.now());
        jpaRepository.save(newRecord);

        double deltaWeight = currentRecord != null ? bestSet.weight - currentRecord.getWeight() : 0;
        int deltaReps = currentRecord != null ? bestSet.reps - currentRecord.getReps() : 0;

        results.add(new PersonalRecordResult(exercise.getId(), bestSet.weight, bestSet.reps, recordType, deltaWeight, deltaReps));
      }
    }

    return results;
  }

  private BestSet findBestSet(List<Workout.Exercise.Set> sets) {
    if (sets == null || sets.isEmpty()) return null;

    double maxWeight = Double.MIN_VALUE;
    short maxReps = 0;

    for (Workout.Exercise.Set set : sets) {
      if (set.getWeight() > maxWeight) {
        maxWeight = set.getWeight();
        maxReps = set.getReps();
      } else if (set.getWeight() == maxWeight && set.getReps() > maxReps) {
        maxReps = set.getReps();
      }
    }

    return new BestSet(maxWeight, maxReps);
  }

  private RecordType getRecordType(MemberPersonalRecordEntity currentRecord, BestSet bestSet) {
    if (currentRecord == null) return RecordType.WEIGHT;

    if (bestSet.weight > currentRecord.getWeight()) return RecordType.WEIGHT;
    if (bestSet.weight == currentRecord.getWeight() && bestSet.reps > currentRecord.getReps()) return RecordType.REPS;

    return null;
  }

  private record BestSet(double weight, short reps) {}
}
