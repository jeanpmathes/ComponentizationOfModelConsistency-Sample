package tools.vitruv.methodologisttemplate.vsum;

import neojoin.viewtypes.model_identity.ModelIdentityFactory;
import neojoin.viewtypes.model_identity.ModelIdentityViewType;
import neojoin.viewtypes.model_identity.System;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.methodologisttemplate.model.model2.Root;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ModelIdentityViewTypeTest extends AbstractTest {
    @Test
    void insertComponent(@TempDir Path tempDir) throws IOException {
        VirtualModel vsum = createVirtualModel(tempDir);

        addSystem(vsum, tempDir);
        addComponent(vsum, "Component");

        Assertions.assertTrue(assertView(getView(vsum), (View v) -> {
            var system = v.getRootObjects(System.class).iterator().next();
            return system.getComponents().size() == 1
                    && system.getComponents().getFirst().getName().equals("Component");
        }));
    }

    @Test
    void insertRouter(@TempDir Path tempDir) throws IOException {
        VirtualModel vsum = createVirtualModel(tempDir);

        addSystem(vsum, tempDir);
        addRouter(vsum, "Router");

        Assertions.assertTrue(assertView(getView(vsum), (View v) -> {
            var system = v.getRootObjects(System.class).iterator().next();
            return system.getComponents().size() == 1
                    && system.getComponents().getFirst().getName().equals("Router");
        }));
    }

    @Test
    void renameComponent(@TempDir Path tempDir) throws IOException {
        VirtualModel vsum = createVirtualModel(tempDir);

        addSystem(vsum, tempDir);
        addComponent(vsum, "OldName");

        modifyView(getView(vsum).withChangeDerivingTrait(), (CommittableView v) -> {
            var system = v.getRootObjects(System.class).iterator().next();
            system.getComponents().getFirst().setName("NewName");
        });

        Assertions.assertTrue(assertView(getView(vsum), (View v) -> {
            var system = v.getRootObjects(System.class).iterator().next();
            return system.getComponents().size() == 1
                    && system.getComponents().getFirst().getName().equals("NewName");
        }));

        Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(Root.class)), (View v) -> {
            var root = v.getRootObjects(Root.class).iterator().next();
            return root.getEntities().getFirst().getName().equals("NewName");
        }));
    }

    @Test
    void deleteComponent(@TempDir Path tempDir) throws IOException {
        VirtualModel vsum = createVirtualModel(tempDir);

        addSystem(vsum, tempDir);
        addComponent(vsum);

        modifyView(getView(vsum).withChangeDerivingTrait(), (CommittableView v) -> {
            var system = v.getRootObjects(System.class).iterator().next();
            system.getComponents().removeFirst();
        });

        Assertions.assertTrue(assertView(getView(vsum), (View v) -> {
            var system = v.getRootObjects(System.class).iterator().next();
            return system.getComponents().isEmpty();
        }));

        Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(Root.class)), (View v) -> {
            var root = v.getRootObjects(Root.class).iterator().next();
            return root.getEntities().isEmpty();
        }));
    }

    @Test
    void testLink(@TempDir Path tempDir) throws IOException {
        VirtualModel vsum = createVirtualModel(tempDir);

        addSystem(vsum, tempDir);

        modifyView(getView(vsum).withChangeDerivingTrait(), (CommittableView v) -> {
            var system = v.getRootObjects(System.class).iterator().next();

            var component1 = ModelIdentityFactory.eINSTANCE.createComponent();
            component1.setName("Component1");
            var component2 = ModelIdentityFactory.eINSTANCE.createComponent();
            component2.setName("Component2");
            system.getComponents().addAll(List.of(component1, component2));

            var protocol = ModelIdentityFactory.eINSTANCE.createProtocol();
            protocol.setName("ExampleProtocol");
            system.getProtocols().add(protocol);

            var link = ModelIdentityFactory.eINSTANCE.createLink();
            system.getLinks().add(link);
            link.setProtocol(protocol);
            link.getComponents().addAll(List.of(component1, component2));
        });

        Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(Root.class)), (View v) -> {
            var root = v.getRootObjects(Root.class).iterator().next();
            return root.getLinks().size() == 1
                    && root.getLinks().getFirst().getEntities().size() == 2
                    && root.getLinks().getFirst().getEntities().stream()
                           .allMatch(c -> c.getName().startsWith("Component"));
        }));
    }

    protected void addSystem(VirtualModel vsum, Path projectPath) {
        modifyView(getView(vsum).withChangeDerivingTrait(), (CommittableView v) -> {
            var system = ModelIdentityFactory.eINSTANCE.createSystem();
            v.registerRoot(system, URI.createFileURI(projectPath.toString() + "/example.view"));
        });
    }

    protected void addComponent(VirtualModel vsum, String name) {
        modifyView(getView(vsum).withChangeDerivingTrait(), (CommittableView v) -> {
            var component = ModelIdentityFactory.eINSTANCE.createComponent();
            component.setName(name);

            var system = v.getRootObjects(System.class).iterator().next();
            system.getComponents().add(component);
        });
    }

    private VirtualModel createVirtualModel(Path tempDir) throws IOException {
        return createDefaultVirtualModel(tempDir, List.of(new ModelIdentityViewType()));
    }

    private View getView(VirtualModel vsum) {
        return vsum.createSelector(vsum.getViewTypes().stream()
                                       .filter(viewType -> viewType.getName().equals(ModelIdentityViewType.NAME))
                                       .findAny().orElseThrow()).createView();
    }
}
