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

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<TimeRange> conflictingTimes = findConflictingTimes(events, request.getAttendees());
    List<TimeRange> possibleTimes = findPossibleTimes(conflictingTimes, request.getDuration());

    return possibleTimes;
  }

  public List<TimeRange> findConflictingTimes(Collection<Event> events,
      Collection<String> attendees) {
    List<TimeRange> conflicts = findConflicts(events, attendees);
    List<TimeRange> conflictingTimes = mergeOverlappingRanges(conflicts);

    return conflictingTimes;
  }

  public List<TimeRange> findConflicts(Collection<Event> events,
      Collection<String> attendees) {
    List<TimeRange> conflicts = new ArrayList<>();

    for (Event event : events) {
      for (String eventAttendee : event.getAttendees()) {
        if (attendees.contains(eventAttendee)) {
          conflicts.add(event.getWhen());
          break;
        }
      }
    }

    Collections.sort(conflicts, TimeRange.ORDER_BY_START);

    return conflicts;
  }

  public List<TimeRange> mergeOverlappingRanges(List<TimeRange> conflicts) {
    List<TimeRange> conflictingTimes = new ArrayList<>();
    TimeRange first = null;
    TimeRange second = null;
    TimeRange combined = null;
    boolean nested = false;
    boolean useCombinedRange = false;

    for (int i = 0; i < conflicts.size() - 1; i++) {
      if (useCombinedRange) {
        first = combined;
        useCombinedRange = false;
      } else {
        first = conflicts.get(i);
      }

      second = conflicts.get(i + 1);
      nested = false;

      if (!first.overlaps(second)) {
        conflictingTimes.add(first);
      } else if (!first.contains(second)) {
        combined = TimeRange.fromStartEnd(first.start(), second.end(), false);
        useCombinedRange = true;
      } else {
        conflictingTimes.add(first);
        nested = true;

        // Skip over adding the second TimeRange
        i++;
      }
    }

    if (conflicts.size() == 0 || nested) {
      return conflictingTimes;
    }

    if (conflicts.size() == 1) {
      conflictingTimes.add(conflicts.get(0));
    } else if (useCombinedRange) {
      conflictingTimes.add(combined);
    } else {
      conflictingTimes.add(second);
    }

    return conflictingTimes;
  }

  public List<TimeRange> findPossibleTimes(List<TimeRange> conflictingTimes, long meetingDuration) {
    List<TimeRange> possibleTimes = new ArrayList<>();

    if (conflictingTimes.isEmpty()) {
      addTimeIfLongEnough(TimeRange.WHOLE_DAY, meetingDuration, possibleTimes);
      return possibleTimes;
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

    return possibleTimes;
  }

  public void addTimeIfLongEnough(TimeRange time, long minDuration, List<TimeRange> timeList) {
    if (time.duration() >= minDuration) {
      timeList.add(time);
    }
  }
}
