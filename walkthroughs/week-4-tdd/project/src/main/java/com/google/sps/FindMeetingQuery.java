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
import java.util.*;

public final class FindMeetingQuery {
    //merge list sorted of time ranges into shortest list possible
    private ArrayList<TimeRange> condenseRanges(TreeSet<TimeRange> originalRanges){
        ArrayList<TimeRange> mergedBusy = new ArrayList<TimeRange>();
        TimeRange max = originalRanges.first(); //keeps track of merged busy time ranges throught the loop  
        for (TimeRange value : originalRanges) {
            if (max.overlaps(value)){ 
                //if the current time range ends after merged time range, update the end of merged time range
                if (value.end() > max.end()){ 
                    max = TimeRange.fromStartEnd(max.start(), value.end(),/*inclusive=*/ false);
                }
            }
            else {
                mergedBusy.add(max);
                max = value;
            }   
        }
        mergedBusy.add(max);
        return mergedBusy;
    }


    //calculate the free times based on merged busy times 
    private Collection<TimeRange> findFreeTimes(ArrayList<TimeRange> mergedRanges, MeetingRequest request){  
        Collection<TimeRange> freeTimes = new ArrayList<TimeRange>();
        //check if the time between the start of the day and the first busy range will work 
        int range = 0;
        range = mergedRanges.get(0).start(); //start of first time range
        if (request.getDuration() <= range) {
            TimeRange beforeBusy = TimeRange.fromStartDuration(TimeRange.START_OF_DAY, range);
            freeTimes.add(beforeBusy);
        }
        for (int i = 0; i < mergedRanges.size(); i ++) {
            //check if the time ranges between the inner busy times of the day will work 
            if ((i != mergedRanges.size() - 1) && ((i + 1) < mergedRanges.size())) { //if not on the last range , get time ranges in between 
                range = mergedRanges.get(i + 1).start() - mergedRanges.get(i).end();
                if (request.getDuration() <= range) {
                    TimeRange inbetween = TimeRange.fromStartDuration(mergedRanges.get(i).end(), range);
                    freeTimes.add(inbetween);
                }
            }

        }
        //check if the time range between the last busy time of the day and the end of the day will work
        int lastIndex = mergedRanges.size() - 1;
        range = TimeRange.END_OF_DAY + 1 - mergedRanges.get(lastIndex).end();
        if (request.getDuration() <= range) {
            TimeRange afterBusy = TimeRange.fromStartDuration(mergedRanges.get(lastIndex).end(), range);
            freeTimes.add(afterBusy);
        }
        
        return freeTimes;
    }

    //returns a list of possible times for a meeting given the events already schedule for the day and a meeting request
    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        Collection<String> meetingAttendees = request.getAttendees();
        Collection<TimeRange> freeFrom = new ArrayList<TimeRange>();
        TreeSet<TimeRange> busyFrom = new TreeSet(TimeRange.ORDER_BY_START); //all times of the day where meeting attendees' are unavailable    
        //if a request is longer than a full day, no free times
        if (request.getDuration() > TimeRange.END_OF_DAY + 1) { 
            return freeFrom;
        }
        //if there are no meeting attendees, then whole day is free 
        if (meetingAttendees.isEmpty()){
            freeFrom.add(TimeRange.WHOLE_DAY);
            return freeFrom;
        }
        /*otherwise, iterate through all events to find the ones relevent to the meeting attendees
        and add the times of those relevant meetings to the busyFrom ArrayList*/
        for(Event e : events) {
            Set<String> evAttendees = e.getAttendees();
            for(String a : meetingAttendees) {
                if (evAttendees.contains(a)) {
                        busyFrom.add(e.getWhen());
                        break;
                }    
            }
        }
        //if there are no events already scheduled for the people in the meeting request, then whole day is free
        if (busyFrom.isEmpty()){
            freeFrom.add(TimeRange.WHOLE_DAY);
            return freeFrom;
        }
        //sort the busy times in order to condense them as much as possible into the mergedBusy ArrayList      
        ArrayList<TimeRange> mergedBusy = new ArrayList<TimeRange>();
        mergedBusy = condenseRanges(busyFrom);
        freeFrom = findFreeTimes(mergedBusy, request);
        return freeFrom;
    }

}