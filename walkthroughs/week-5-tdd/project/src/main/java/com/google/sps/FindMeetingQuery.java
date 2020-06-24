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
import java.util.Collection;
import java.util.Collections;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ImmutableList<TimeRange> conflictingTimes =
        findConflictingTimes(events, request.getAttendees());
    ImmutableList<TimeRange> possibleTimes =
        findPossibleTimes(conflictingTimes, request.getDuration());

    return possibleTimes;
  }

  private ImmutableList<TimeRange> findConflictingTimes(
      Collection<Event> events, Collection<String> attendees) {
    ImmutableList<TimeRange> conflicts = findConflicts(events, attendees);
    ImmutableList<TimeRange> conflictingTimes = mergeOverlappingRanges(conflicts);

    return conflictingTimes;
  }

  private ImmutableList<TimeRange> findConflicts(
      Collection<Event> events, Collection<String> requestAttendees) {
    ImmutableList.Builder<TimeRange> builder = new ImmutableList.Builder<TimeRange>();

    for (Event event : events) {
      if (!Collections.disjoint(requestAttendees, event.getAttendees())) {
        builder.add(event.getWhen());
      }
    }

    return builder.build();
  }

  private ImmutableList<TimeRange> mergeOverlappingRanges(ImmutableList<TimeRange> conflicts) {
    ImmutableList<TimeRange> sortedConflicts = ImmutableList.sortedCopyOf(
        TimeRange.ORDER_BY_START.thenComparing(TimeRange.ORDER_BY_END.reversed()), conflicts);

    ImmutableList<TimeRange> unnestedConflicts = removeNestedTimeRanges(sortedConflicts);

    ImmutableList.Builder<TimeRange> builder = new ImmutableList.Builder<TimeRange>();
    int startIndex = 0;

    while (startIndex < unnestedConflicts.size()) {
      int endIndex = findEndOfOverlap(unnestedConflicts, startIndex);

      TimeRange startRange = unnestedConflicts.get(startIndex);
      TimeRange endRange = unnestedConflicts.get(endIndex - 1);
      TimeRange overlap = TimeRange.fromStartEnd(startRange.start(), endRange.end(), false);

      builder.add(overlap);

      startIndex = endIndex;
    }

    return builder.build();
  }

  private ImmutableList<TimeRange> removeNestedTimeRanges(ImmutableList<TimeRange> conflicts) {
    ImmutableList.Builder<TimeRange> builder = new ImmutableList.Builder<TimeRange>();

    if (conflicts.isEmpty()) {
      return builder.build();
    }

    TimeRange lastAdded = conflicts.get(0);
    builder.add(lastAdded);

    for (TimeRange conflict : conflicts) {
      if (!lastAdded.contains(conflict)) {
        builder.add(conflict);
        lastAdded = conflict;
      }
    }

    return builder.build();
  }

  private int findEndOfOverlap(ImmutableList<TimeRange> conflicts, int startIndex) {
    for (int endIndex = startIndex + 1; endIndex < conflicts.size(); endIndex++) {
      if (!conflicts.get(endIndex - 1).overlaps(conflicts.get(endIndex))) {
        return endIndex;
      }
    }

    return conflicts.size();
  }

  private ImmutableList<TimeRange> findPossibleTimes(
      ImmutableList<TimeRange> conflictingTimes, long meetingDuration) {
    ImmutableList<TimeRange> possibleTimes = new ImmutableList.Builder<TimeRange>().build();

    if (conflictingTimes.isEmpty()) {
      possibleTimes = addTimeIfLongEnough(TimeRange.WHOLE_DAY, meetingDuration, possibleTimes);
      return possibleTimes;
    }

    TimeRange initial = conflictingTimes.get(0);
    TimeRange valid = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, initial.start(), false);
    possibleTimes = addTimeIfLongEnough(valid, meetingDuration, possibleTimes);

    for (int i = 0; i < conflictingTimes.size() - 1; i++) {
      TimeRange first = conflictingTimes.get(i);
      TimeRange second = conflictingTimes.get(i + 1);

      valid = TimeRange.fromStartEnd(first.end(), second.start(), false);
      possibleTimes = addTimeIfLongEnough(valid, meetingDuration, possibleTimes);
    }

    TimeRange last = conflictingTimes.get(conflictingTimes.size() - 1);
    valid = TimeRange.fromStartEnd(last.end(), TimeRange.END_OF_DAY, true);
    possibleTimes = addTimeIfLongEnough(valid, meetingDuration, possibleTimes);

    return possibleTimes;
  }

  private ImmutableList<TimeRange> addTimeIfLongEnough(
      TimeRange time, long minDuration, ImmutableList<TimeRange> timeList) {
    ImmutableList.Builder<TimeRange> builder = new ImmutableList.Builder<TimeRange>();

    builder.addAll(timeList);

    if (time.duration() >= minDuration) {
      builder.add(time);
    }

    return builder.build();
  }
}
