// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<TimeRange> conflictingTimes = findConflictingTimes(events, request.getAttendees());
    List<TimeRange> possibleTimes = findPossibleTimes(conflictingTimes, request.getDuration());

    return possibleTimes;
  }

  private ImmutableList<TimeRange> findConflictingTimes(
      Collection<Event> events, Collection<String> attendees) {
    List<TimeRange> conflicts = findConflicts(events, attendees);
    List<TimeRange> conflictingTimes = mergeOverlappingRanges(conflicts);

    return ImmutableList.copyOf(conflictingTimes);
  }

  private ImmutableList<TimeRange> findConflicts(
      Collection<Event> events, Collection<String> requestAttendees) {
    List<TimeRange> conflicts = new ArrayList<>();

    for (Event event : events) {
      if (!Collections.disjoint(requestAttendees, event.getAttendees())) {
        conflicts.add(event.getWhen());
      }
    }

    return ImmutableList.sortedCopyOf(
        TimeRange.ORDER_BY_START.thenComparing(TimeRange.ORDER_BY_END.reversed()), conflicts);
  }

  private ImmutableList<TimeRange> mergeOverlappingRanges(List<TimeRange> conflicts) {
    List<TimeRange> unnestedConflicts = removeNestedTimeRanges(conflicts);
    List<TimeRange> conflictingTimes = new ArrayList<>();
    int startIndex = 0;

    while (startIndex < unnestedConflicts.size()) {
      int endIndex = findEndOfOverlap(unnestedConflicts, startIndex);

      TimeRange startRange = unnestedConflicts.get(startIndex);
      TimeRange endRange = unnestedConflicts.get(endIndex - 1);
      TimeRange overlap = TimeRange.fromStartEnd(startRange.start(), endRange.end(), false);

      conflictingTimes.add(overlap);

      startIndex = endIndex;
    }

    return ImmutableList.copyOf(conflictingTimes);
  }

  private ImmutableList<TimeRange> removeNestedTimeRanges(List<TimeRange> conflicts) {
    List<TimeRange> unnestedConflicts = new ArrayList<>();

    if (conflicts.isEmpty()) {
      return ImmutableList.copyOf(unnestedConflicts);
    }

    unnestedConflicts.add(conflicts.get(0));

    for (TimeRange conflict : conflicts) {
      if (!unnestedConflicts.get(unnestedConflicts.size() - 1).contains(conflict)) {
        unnestedConflicts.add(conflict);
      }
    }

    return ImmutableList.copyOf(unnestedConflicts);
  }

  private int findEndOfOverlap(List<TimeRange> conflicts, int startIndex) {
    for (int endIndex = startIndex + 1; endIndex < conflicts.size(); endIndex++) {
      if (!conflicts.get(endIndex - 1).overlaps(conflicts.get(endIndex))) {
        return endIndex;
      }
    }

    return conflicts.size();
  }

  private ImmutableList<TimeRange> findPossibleTimes(
      List<TimeRange> conflictingTimes, long meetingDuration) {
    List<TimeRange> possibleTimes = new ArrayList<>();

    if (conflictingTimes.isEmpty()) {
      addTimeIfLongEnough(TimeRange.WHOLE_DAY, meetingDuration, possibleTimes);
      return ImmutableList.copyOf(possibleTimes);
    }

    TimeRange initial = conflictingTimes.get(0);
    TimeRange valid = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, initial.start(), false);
    addTimeIfLongEnough(valid, meetingDuration, possibleTimes);

    for (int i = 0; i < conflictingTimes.size() - 1; i++) {
      TimeRange first = conflictingTimes.get(i);
      TimeRange second = conflictingTimes.get(i + 1);

      valid = TimeRange.fromStartEnd(first.end(), second.start(), false);
      addTimeIfLongEnough(valid, meetingDuration, possibleTimes);
    }

    TimeRange last = conflictingTimes.get(conflictingTimes.size() - 1);
    valid = TimeRange.fromStartEnd(last.end(), TimeRange.END_OF_DAY, true);
    addTimeIfLongEnough(valid, meetingDuration, possibleTimes);

    return ImmutableList.copyOf(possibleTimes);
  }

  private void addTimeIfLongEnough(TimeRange time, long minDuration, List<TimeRange> timeList) {
    if (time.duration() >= minDuration) {
      timeList.add(time);
    }
  }
}
