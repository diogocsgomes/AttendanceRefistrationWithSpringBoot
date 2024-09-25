package pt.isec.pd.tp_pd.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import pt.isec.pd.tp_pd.database.DatabaseManager;
import pt.isec.pd.tp_pd.utils.InputValidator;

import java.sql.SQLException;

@RestController
@RequestMapping("attendances")
public class AttendancesController {
    private DatabaseManager _db;
    private InputValidator _validator;

    public AttendancesController() {
        _validator = new InputValidator();
        try {
            this._db = DatabaseManager.getInstance(null);
        } catch (SQLException e) {
            System.err.println("Registration controller cound create db manager.");
        }
    }

    @PostMapping("{code}")
    public ResponseEntity<String> register(
            Authentication authentication,
            @PathVariable("code") String code) {
        // save attendance to database
        int userId = _db.getUserIdFromEmail(authentication.getName());
        boolean isRegistered = _db.registerAttendence(code, userId);

        // resend response with given status
        if (isRegistered)
            return new ResponseEntity<>(HttpStatus.OK);
        else
            return new ResponseEntity<>("Attendance was not registered.",
                    HttpStatus.NOT_FOUND);
    }
}
