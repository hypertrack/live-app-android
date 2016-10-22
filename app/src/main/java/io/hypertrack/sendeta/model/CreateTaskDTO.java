package io.hypertrack.sendeta.model;

/**
 * Created by piyush on 22/10/16.
 */
public class CreateTaskDTO {

    private String action = "pickup";
    private CreateDestinationDTO destination;

    public CreateDestinationDTO getDestination() {
        return destination;
    }

    public void setDestination(CreateDestinationDTO destination) {
        this.destination = destination;
    }

    public CreateTaskDTO(CreateDestinationDTO destination) {
        this.destination = destination;
    }
}
