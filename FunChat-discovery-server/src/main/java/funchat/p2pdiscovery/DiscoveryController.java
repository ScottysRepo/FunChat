package funchat.p2pdiscovery;

import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/discovery")
public class DiscoveryController {

    // Maps username to ip port
    private final ConcurrentHashMap<String, String> userMap = new ConcurrentHashMap<>();

    @RequestMapping("/stats")
    public String stats() {
        return "We have " + userMap.size() + " registered users!";
    }

    //assume the discovery server runs on port 8080
    // The client tells us what chat port its gonna use
    @PostMapping("/register")
    public void register(@RequestParam String id,
                         @RequestParam int chatPort,
                         HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        userMap.put(id, ip + ":" + chatPort);
        System.out.println("Registered " + id + " -> " + ip + ":" + chatPort);
    }

    @GetMapping("/search")
    public String search(@RequestParam String id) {
        return userMap.get(id); // "ip:port" or null
    }
}
