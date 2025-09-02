package model;

import java.util.ArrayList;
import java.util.List;

public class GroupedEventView {
    private String title;
    private List<String> scheduleOptions; 

    public GroupedEventView(String title) {
        this.title = title;
        this.scheduleOptions = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public List<String> getScheduleOptions() {
        return scheduleOptions;
    }

    public String getScheduleAsString() {
        return String.join(", ", scheduleOptions);
    }

    public void addSchedule(String day, String venue) {
        scheduleOptions.add(day + " - " + venue);
    }
}
