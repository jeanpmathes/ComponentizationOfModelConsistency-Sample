package tools.vitruv.methodologisttemplate.vsum;

import neojoin.viewtypes.example.ExampleFactory;
import neojoin.viewtypes.example.ExampleViewType;
import neojoin.viewtypes.example.Root;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.methodologisttemplate.model.model.ModelFactory;
import tools.vitruv.methodologisttemplate.model.model.System;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ExampleViewTypeTest extends AbstractTest {
    @Test
    void deleteComponentUsingExampleView(@TempDir Path tempDir) throws IOException {
        VirtualModel vsum = createVirtualModel(tempDir);
        addSystem(vsum, tempDir);

        modifyView(getDefaultView(vsum, List.of(System.class)).withChangeRecordingTrait(), (CommittableView v) -> {
            var system = v.getRootObjects(System.class).iterator().next();

            var component1 = ModelFactory.eINSTANCE.createComponent();
            component1.setName("Component1");
            system.getComponents().add(component1);

            var component2 = ModelFactory.eINSTANCE.createComponent();
            component2.setName("Component2");
            system.getComponents().add(component2);
        });

        modifyView(getView(vsum).withChangeDerivingTrait(), (CommittableView v) -> {
            Root root = v.getRootObjects(Root.class).iterator().next();

            root.getAllThings().removeFirst();
        });

        Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(System.class)), (View v) -> {
            var system = v.getRootObjects(System.class).iterator().next();
            return system.getComponents().size() == 1
                    && system.getComponents().getFirst().getName().equals("Component2");
        }));
    }

    @Test
    void addComponentUsingExampleView(@TempDir Path tempDir) throws IOException {
        VirtualModel vsum = createVirtualModel(tempDir);
        addSystem(vsum, tempDir);

        modifyView(getView(vsum).withChangeDerivingTrait(), (CommittableView v) -> {
            Root root = v.getRootObjects(Root.class).iterator().next();

            root.getAllThings().add(ExampleFactory.eINSTANCE.createThing());
            root.getAllThings().add(ExampleFactory.eINSTANCE.createThing());
        });

        Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(System.class)), (View v) -> {
            var system = v.getRootObjects(System.class).iterator().next();
            return system.getComponents().size() == 2;
        }));
    }

    @Test
    void renameComponentUsingExampleView(@TempDir Path tempDir) throws IOException {
        VirtualModel vsum = createVirtualModel(tempDir);
        addSystem(vsum, tempDir);
        addComponent(vsum, "OldName1");
        addComponent(vsum, "OldName2");

        modifyView(getView(vsum).withChangeDerivingTrait(), (CommittableView v) -> {
            Root root = v.getRootObjects(Root.class).iterator().next();
            root.getAllThings().get(0).setName("NewName1");
            root.getAllThings().get(1).setName("NewName2");
        });

        Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(System.class)), (View v) -> {
            var system = v.getRootObjects(System.class).iterator().next();
            return system.getComponents().size() == 2
                    && system.getComponents().get(0).getName().equals("NewName1")
                    && system.getComponents().get(1).getName().equals("NewName2");
        }));
    }

    private VirtualModel createVirtualModel(Path tempDir) throws IOException {
        return createDefaultVirtualModel(tempDir, List.of(new ExampleViewType()));
    }

    private View getView(VirtualModel vsum) {
        return vsum.createSelector(vsum.getViewTypes().stream()
                                       .filter(viewType -> viewType.getName().equals(ExampleViewType.NAME)).findAny()
                                       .orElseThrow()).createView();
    }
}
