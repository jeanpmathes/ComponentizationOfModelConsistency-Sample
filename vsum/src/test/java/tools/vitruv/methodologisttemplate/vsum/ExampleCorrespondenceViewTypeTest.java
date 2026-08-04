package tools.vitruv.methodologisttemplate.vsum;

import neojoin.viewtypes.example_correspondence.ExampleCorrespondenceViewType;
import neojoin.viewtypes.example_correspondence.Thing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.vsum.VirtualModel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ExampleCorrespondenceViewTypeTest extends AbstractTest {
    @Test
    void testGetFromCorrespondenceView(@TempDir Path tempDir) throws IOException {
        VirtualModel vsum = createVirtualModel(tempDir);

        Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(Thing.class)),
                                         (View v) -> true
        ));
    }

    private VirtualModel createVirtualModel(Path tempDir) throws IOException {
        return createDefaultVirtualModel(tempDir, List.of(new ExampleCorrespondenceViewType()));
    }

    private View getView(VirtualModel vsum) {
        return vsum.createSelector(vsum.getViewTypes()
                                           .stream()
                                           .filter(viewType -> viewType.getName()
                                                   .equals(ExampleCorrespondenceViewType.NAME))
                                           .findAny()
                                           .orElseThrow()).createView();
    }
}
