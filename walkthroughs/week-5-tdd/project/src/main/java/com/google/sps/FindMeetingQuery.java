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
import java.util.ArrayList;
import java.util.List;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<TimeRange> possibleTimes = new ArrayList<>();
    List<TimeRange> conflictingTimes = new ArrayList<>();
    Collection<String> attendees = request.getAttendees();

    // Find every TimeRange that needs to be avoided
    for (Event event : events) {
      for (String attendee : event.getAttendees()) {
        if (attendees.contains(attendee)) {
          conflictingTimes.add(event.getWhen());
          break;
        }
      }
    }

    return possibleTimes;
  }
}
