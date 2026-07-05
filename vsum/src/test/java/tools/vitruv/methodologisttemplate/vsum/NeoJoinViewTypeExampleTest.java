package tools.vitruv.methodologisttemplate.vsum;

import neojoin.viewtypes.example.ExampleFactory;
import neojoin.viewtypes.example.ExampleViewType;
import neojoin.viewtypes.example.Root;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.views.ViewSelector;
import tools.vitruv.framework.views.ViewType;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.methodologisttemplate.model.model.ModelFactory;
import tools.vitruv.methodologisttemplate.model.model.System;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public class NeoJoinViewTypeExampleTest extends AbstractTest {
    @Test
    void deleteComponentUsingExampleView(@TempDir Path tempDir) {
        VirtualModel vsum = createDefaultVirtualModel(tempDir);
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

        modifyView(getView(vsum, ExampleViewType::new).withChangeDerivingTrait(), (CommittableView v) -> {
            Root root = v.getRootObjects(Root.class).iterator().next();

            root.getAllThings().remove(0);
        });

        Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(System.class)), (View v) -> {
            var system = v.getRootObjects(System.class).iterator().next();
            return system.getComponents().size() == 1 && system.getComponents().get(0).getName().equals("Component2");
        }));
    }

    @Test
    void addComponentUsingExampleView(@TempDir Path tempDir) {
        VirtualModel vsum = createDefaultVirtualModel(tempDir);
        addSystem(vsum, tempDir);

        modifyView(getView(vsum, ExampleViewType::new).withChangeDerivingTrait(), (CommittableView v) -> {
            Root root = v.getRootObjects(Root.class).iterator().next();

            root.getAllThings().add(ExampleFactory.eINSTANCE.createThing());
            root.getAllThings().add(ExampleFactory.eINSTANCE.createThing());
        });

        Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(System.class)), (View v) -> {
            var system = v.getRootObjects(System.class).iterator().next();
            return system.getComponents().size() == 2;
        }));
    }

    private View getView(VirtualModel vsum, Supplier<ViewType<? extends ViewSelector>> viewTypeSupplier) {

        return vsum.createSelector(viewTypeSupplier.get()).createView();
    }
}
