package site.matzip.config.debug;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class WhoAmIController {
    @GetMapping("/whoami")
    @PreAuthorize("isAuthenticated()")
    public Map<String,Object> whoami(Authentication a){
        return Map.of("name", a==null?null:a.getName());
    }
}
