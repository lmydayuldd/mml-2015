package privacy;

import javafx.scene.control.Labeled;
import learning.LabeledSample;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * Created by aspis on 25.03.2015.
 */
public class NoisyQueryableFactoryTest {


    @Test
    public void buildNoisyQueryble(){
        NoisyQueryableFactory factory = new NoisyQueryableFactory();
        Budget agent = mock(Budget.class);
        List<LabeledSample> data = mock(List.class);

        NoisyQueryable<LabeledSample> queryable = factory.getQueryable(agent, data);

        assertNotNull(queryable);
        assertEquals(agent, queryable.getAgent());
    }

}