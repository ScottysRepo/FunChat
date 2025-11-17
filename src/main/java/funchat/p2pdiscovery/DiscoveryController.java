package funchat.p2pdiscovery;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@RestController()
@RequestMapping("/discovery")
public class DiscoveryController {

    //Todo: Turn this inot a database or something, or at the very least, save this as a CSV
    private final ConcurrentHashMap<String , String> userMap = new ConcurrentHashMap<>();



    @Override
    public String toString() {
        return super.toString();
    }

    @RequestMapping("/stats")
    public String Stats()
    {
        return "We have " + userMap.size() + " registered users!";
    }

    //We assume the same port always for discovery which is 5050.
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public void Register(@RequestParam String id, HttpServletRequest request)
    {
        userMap.put(id, request.getRemoteAddr() + "::" + request.getRemotePort());
    }

    //TODO: add a thing if the name is wrong. Maybe even do a close name?
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String Search(@RequestParam String id)
    {
        return userMap.get(id);
    }


}
