package pt.isec.pd.tp_pd.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import pt.isec.pd.tp_pd.data.Event;
import pt.isec.pd.tp_pd.data.User;
import pt.isec.pd.tp_pd.database.DatabaseManager;
import pt.isec.pd.tp_pd.utils.InputValidator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

@RestController
@RequestMapping("events")
public class EventsController {
    private DatabaseManager _db;
    private final InputValidator _validator;

    public EventsController() {
        _validator = new InputValidator();
        try {
            this._db = DatabaseManager.getInstance(null);
        } catch (SQLException e) {
            System.err.println("Registration controller could not create db manager.");
        }
    }

    @PostMapping
    public ResponseEntity<String> createEvent(
            Authentication authentication, @RequestBody Event event) {

        // event validation
        try {
            _validator.validateEvent(event);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        // check if admin makes a request
        if (!authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("SCOPE_ADMIN"))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // save event to database
        Event insertedEvent = _db.insertEvent(
                event.getName(), event.getPlace(),
                event.getEventDate(), event.getStartHour(),
                event.getEndHour(), event.getCreatorId(),
                event.getCode(), event.getExpirationCodeDate()
        );

        // resend response with given status
        if (insertedEvent != null)
            return new ResponseEntity<>("Event added.", HttpStatus.OK);
        else
            return new ResponseEntity<>(
                    "User with given id or email already exists",
                    HttpStatus.CONFLICT
            );
    }

    @GetMapping("code")
    public ResponseEntity<String> generateCode(
            Authentication authentication) {

        // check if admin makes a request
        if (!authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("SCOPE_ADMIN"))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // generate code
        String code;
        boolean doesCodeExist = true;
        code = generateRandomString();

        for (int i = 0; i < 5; ++i) { // make for loop not to create deadlock
            doesCodeExist = _db.doesCodeExist(code);
            if (!doesCodeExist) break;
        }
        if (doesCodeExist) code = null;

        // resend response with given status
        if (code != null)
            return new ResponseEntity<>(code, HttpStatus.OK);
        else
            return new ResponseEntity<>(
                    "Code could not be generated.",
                    HttpStatus.CONFLICT
            );
    }

    @GetMapping("{id}/users")
    public ResponseEntity<ArrayList<User>> getEventUsers(
            Authentication authentication, @PathVariable("id") int id) {

        // validation check if provided id is possible
        if (id < 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        // check if admin makes a request
        if (!authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("SCOPE_ADMIN"))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }


        // get event's users from database
        ArrayList<User> users = _db.listEventUsers(id);

        // resend response with given status
        if (users != null)
            return new ResponseEntity<>(users, HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @GetMapping
    public ResponseEntity<ArrayList<Event>> getCreatedEvents(Authentication authentication) {
        // check if admin makes a request
        if (!authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("SCOPE_ADMIN"))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // get event's users from database
        int userId = _db.getUserIdFromEmail(authentication.getName());
        ArrayList<Event> events = _db.listUserEventsPartialData(userId);

        // resend response with given status
        if (events != null)
            return new ResponseEntity<>(events, HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Event> deleteEvent(
            Authentication authentication, @PathVariable("id") int id) {
        // validation check if provided id is possible
        if (id < 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        // check if admin makes a request
        if (!authentication.getAuthorities().contains(
                new SimpleGrantedAuthority("SCOPE_ADMIN"))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // get event's users from database
        boolean deleted = _db.deleteEvent(id);

        // resend response with given status
        if (deleted)
            return new ResponseEntity<>(HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    private String generateRandomString() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder stringBuilder = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            stringBuilder.append(randomChar);
        }

        return stringBuilder.toString();
    }
}
