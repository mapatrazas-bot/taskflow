package taskflow;

import com.taskflow.TaskflowApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test básico que verifica que el contexto de Spring arranca correctamente.
 * Usa classes = TaskflowApplication.class porque este archivo está en el
 * paquete 'taskflow' mientras que @SpringBootApplication está en 'com.taskflow'.
 */
@SpringBootTest(classes = TaskflowApplication.class)
@ActiveProfiles("dev")
class TaskflowApplicationTests {

	@Test
	void contextLoads() {
	}

}
