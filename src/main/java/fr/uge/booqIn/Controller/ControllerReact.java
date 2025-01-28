package fr.uge.booqIn.Controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ControllerReact {
    @GetMapping("reactTest")
    public Square getSquare(){
        return new Square(12,"red");
    }

    public record Square(@JsonProperty("size") int size, @JsonProperty("color") String color){}
}
