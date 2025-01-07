package com.github.matsud224.waveviz;

import java.util.ArrayList;

public class Annotations implements TimeSeries {
    private final ArrayList<String> path;
    private final String type;
    private final ArrayList<Annotation> annotationList = new ArrayList<>();

    public Annotations(ArrayList<String> path, String type, int width) {
        this.path = path;
        this.type = type;
    }

    @Override
    public ArrayList<String> getPath() {
        return path;
    }

    @Override
    public String getType() {
        return type;
    }

    public void addAnnotation(Annotation annotation) {
        annotationList.add(annotation);
    }

    @Override
    public TimeSpan getValue(int time) {
    }

    @Override
    public int getStartTime() {
        return annotationList.stream().mapToInt(x -> x.getTimeRange().getStartTime()).min().orElse(0);
    }

    @Override
    public int getEndTime() {
        return annotationList.stream().mapToInt(x -> x.getTimeRange().getEndTime()).max().orElse(0);
    }

    @Override
    public String toString() {
        return path.get(path.size() - 1);
    }
}
