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
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public final class FindMeetingQuery {
    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        ArrayList<TimeRange> busyFrom = new ArrayList<>();
        Collection<String> meetingAttendees = request.getAttendees();
        Collection<TimeRange> freeFrom = new ArrayList<TimeRange>();
        //if a request is longer than a full day, no free times
        if (request.getDuration() > TimeRange.END_OF_DAY + 1) { 
            return freeFrom;
        }
        //if there are no meeting attendees, then whole day is free 
        if (meetingAttendees.isEmpty()){
            TimeRange allDay = TimeRange.WHOLE_DAY;
            freeFrom.add(allDay);
            return freeFrom;
        }

        /*otherwise, iterate through all events to find the ones relevent to the meeting attendees
        and add the times of those relevant meetings to the busyFrom ArrayList*/
        for(Event e : events) {
            Set<String> evAttendees = e.getAttendees();
            for(String a : meetingAttendees) {
                if (evAttendees.contains(a)) {
                        busyFrom.add(e.getWhen());
                }    
            }
        }
        //if there are no events allready scheduled for the people in the metting request, then whole day is free
        if (busyFrom.isEmpty()){
            TimeRange allDay = TimeRange.WHOLE_DAY;
            freeFrom.add(allDay);
            return freeFrom;
        }
        //sort the busy times in order to condense them as much as possible into the mergedBusy ArrayList      
        Collections.sort(busyFrom, TimeRange.ORDER_BY_START);
        ArrayList<TimeRange> mergedBusy = new ArrayList<TimeRange>();
        TimeRange max = busyFrom.get(0); //keeps track of merged busy time ranges throught the for loop
        for (int i = 1; i < busyFrom.size(); i ++){
            if (max.overlaps(busyFrom.get(i))){ 
                //if the current time range ends after merged time range, update the end of merged time range
                if (busyFrom.get(i).end() > max.end()){ 
                    max = TimeRange.fromStartEnd(max.start(), busyFrom.get(i).end(), false);
                }
            }
            else {
                mergedBusy.add(max);
                max = busyFrom.get(i);

            }   
        }
        mergedBusy.add(max);
        //calculate the free times based on meged busy times 
        for (int i = 0; i < mergedBusy.size(); i ++) {
            int range = 0;
            //check if the time between the start of the day and the first busy range will work 
            if (i == 0) {
                range = mergedBusy.get(i).start(); 
                if (request.getDuration() <= range) {
                    TimeRange beforeBusy = TimeRange.fromStartDuration(TimeRange.START_OF_DAY, range);
                    freeFrom.add(beforeBusy);
                }
            }
            //check if the time ranges between the inner busy times of the day will work 
            if ((i != mergedBusy.size() - 1) && ((i + 1) < mergedBusy.size())) { //if not on the last range , get time ranges in between 
                range = mergedBusy.get(i + 1).start() - mergedBusy.get(i).end();
                if (request.getDuration() <= range) {
                    TimeRange inbetween = TimeRange.fromStartDuration(mergedBusy.get(i).end(), range);
                    freeFrom.add(inbetween);
                }
            }
            //check if the time range bewtween the last busy time of the day and the end of the day will work 
            if (i == mergedBusy.size() - 1) {
                range = TimeRange.END_OF_DAY + 1 - mergedBusy.get(i).end();
                if (request.getDuration() <= range) {
                    TimeRange afterBusy = TimeRange.fromStartDuration(mergedBusy.get(i).end(), range);
                    freeFrom.add(afterBusy);
                }
            }
        }
        return freeFrom;
    }

}