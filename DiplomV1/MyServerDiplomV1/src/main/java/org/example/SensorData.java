package org.example;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class SensorData {

    private Integer sensorId;
    private Double value;
    private String timestamp;

    public SensorData(int sensorId, double value, String timestamp) {
    }
}
