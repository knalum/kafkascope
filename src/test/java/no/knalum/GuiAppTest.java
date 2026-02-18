package no.knalum;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GuiAppTest extends AssertJSwingJUnitTestCase {

    private FrameFixture window;

    @Override
    protected void onSetUp() {
        KafkaScope frame = GuiActionRunner.execute(() -> {
            KafkaScope kafkaScope = new KafkaScope();
            kafkaScope.setSize(1000, 700);
            kafkaScope.setTitle("KafkaScope Test");
            return kafkaScope;
        });
        window = new FrameFixture(robot(), frame);
        window.show();
    }

    @Test
    public void shouldShowConnectToBrokerDialogOnStartup() {
        // Click on File menu
        window.menuItemWithPath("File").click();

        // Click on "Connect to broker..." menu item
        window.menuItemWithPath("Connect to broker...").click();

        // Wait for the dialog to appear
        robot().waitForIdle();

        // Find the dialog by title
        DialogFixture dialog = window.dialog();

        // Assert the dialog is showing
        assertThat(dialog.target().isShowing()).isTrue();
        assertThat(dialog.target().getTitle()).isEqualTo("Connect to Broker");

        // Clean up - close the dialog
        dialog.close();
    }

    @Override
    protected void onTearDown() {
        if (window != null) {
            window.cleanUp();
        }
    }
}
