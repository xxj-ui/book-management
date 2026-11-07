package executor;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoTransactional {



    @Transactional
    public void update(Integer from,Integer to,Double price){
        
    }
}
