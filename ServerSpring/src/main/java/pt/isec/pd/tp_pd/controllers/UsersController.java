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

@RestController
@RequestMapping("users")
public class UsersController {
    private DatabaseManager _db;
    private InputValidator _validator;

    public UsersController() {
        _validator = new InputValidator();
        try {
            this._db = DatabaseManager.getInstance(null);
        } catch (SQLException e) {
            System.err.println("Registration controller cound create db manager.");
        }
    }
    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody User user) {
        // user validation
        if(!_validator.isEmailValid(user.getEmail()))
            return ResponseEntity.badRequest().body("Email is not valid");

        // save user to database
        User registeredUser = _db.insertUser(user.getEmail(), user.getPassword(),
                user.getUser_type(), user.getUser_id());

        // resend response with given status
        if (registeredUser != null)
            return new ResponseEntity<>(HttpStatus.OK);
        else
            return ResponseEntity.badRequest().body("User with given id or email already exists");
    }

    @GetMapping("{id}/events")
    public ResponseEntity<ArrayList<Event>> getUserEvents(
            Authentication authentication, @PathVariable("id") int id) {
        // id validation
        if(id < 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        // check to see if the participant is not trying to take someone else's events
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("PARTICIPANT"))) {
            int userId = _db.getUserIdFromEmail(authentication.getName());
            if (id != userId)
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // save user to database
        ArrayList<Event> events = _db.listAttendances(id);

        // resend response with given status
        if (events != null)
            return new ResponseEntity<>(events, HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("requestAuthor")
    public ResponseEntity<User> getRequestAuthor(
            Authentication authentication) {
        // get request author from database
        int userId = _db.getUserIdFromEmail(authentication.getName());
        User author = _db.getUser(userId);

        // resend response with given status
        if (author != null)
            return new ResponseEntity<>(author, HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
