package tools.vitruv.methodologisttemplate.vsum;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCL;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;
import tools.vitruv.methodologisttemplate.model.model.ModelFactory;
import tools.vitruv.methodologisttemplate.model.model.System;
import tools.vitruv.methodologisttemplate.model.model2.Root;

/**
 * This class provides an example how to define and use a VSUM.
 *
 * <p>It also demonstrates how to integrate VitruviusOCL for constraint-based consistency checking.
 * Constraints are defined in {@code constraints.ocl} and can be evaluated manually via
 * {@link VitruvOCL#evaluateConstraints(java.nio.file.Path)} or automatically after every change
 * propagation by registering a {@link tools.vitruv.change.composite.propagation.ChangePropagationListener}.
 */
public class VSUMExampleTest extends AbstractTest {

  /**
   * Path to the OCL constraint file, located alongside the Reactions in the consistency module.
   *
   * <p>Surefire forks the test JVM with the {@code vsum} module directory (not the repository
   * root) as the working directory, so this path must climb one level up first.
   */
  private static final java.nio.file.Path CONSTRAINT_FILE = java.nio.file.Path.of(
      "../consistency/src/main/constraints/tools/vitruv/methodologisttemplate/consistency/constraints.ocl");

  @BeforeAll
  static void setup() {
    Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
  }

  @Test
  void reloadEmptyVirtualModel(@TempDir Path tempDir) throws IOException {
    InternalVirtualModel vsum = createDefaultVirtualModel(tempDir);
    vsum.dispose();
    vsum = createDefaultVirtualModel(tempDir);
  }

  @Test
  void reloadFilledVirtualModel(@TempDir Path tempDir) throws IOException {
    InternalVirtualModel vsum = createDefaultVirtualModel(tempDir);
    addSystem(vsum, tempDir);
    vsum.dispose();
    vsum = createDefaultVirtualModel(tempDir);
    // Assert that the reloaded virtual model contains the changes we made before disposing it
    Assertions.assertEquals(1, getDefaultView(vsum, List.of(System.class)).getRootObjects().size());
    Assertions.assertEquals(1, getDefaultView(vsum, List.of(Root.class)).getRootObjects().size());
  }

  @Test
  void systemInsertionAndPropagationTest(@TempDir Path tempDir) throws IOException {
    VirtualModel vsum = createDefaultVirtualModel(tempDir);
    addSystem(vsum, tempDir);
    // assert that the directly added System is present
    Assertions.assertEquals(1, getDefaultView(vsum, List.of(System.class)).getRootObjects().size());
    // as well as the Root that should be created by the Reactions, see
    // model2Model2.reactions#14
    Assertions.assertEquals(1, getDefaultView(vsum, List.of(Root.class)).getRootObjects().size());
  }

  @Test
  void insertComponent(@TempDir Path tempDir) throws IOException {
    InternalVirtualModel vsum = createDefaultVirtualModel(tempDir);
    addSystem(vsum, tempDir);
    addComponent(vsum);
    Assertions.assertTrue(
        assertView(
            getDefaultView(vsum, List.of(System.class, Root.class)),
            (View v) -> {
          // assert that a component has been inserted, a entity has been created and that
          // both have the same name
          // Note: to make the test result easier to understand, these different effects
          // should be tested one by one
              return v.getRootObjects(System.class)
                  .iterator()
                  .next()
                  .getComponents()
                  .get(0)
                  .getName()
                  .equals(
                      v.getRootObjects(Root.class)
                          .iterator()
                          .next()
                          .getEntities()
                          .get(0)
                          .getName());
        }));
  }

  @Test
  void insertRouter(@TempDir Path tempDir) throws IOException {
    InternalVirtualModel vsum = createDefaultVirtualModel(tempDir);
    addSystem(vsum, tempDir);
    addRouter(vsum);
    modifyView(
        getDefaultView(vsum, List.of(System.class)).withChangeDerivingTrait(),
        (CommittableView v) -> {
                 // add a router to the system
                 v.getRootObjects(System.class)
                     .iterator()
                     .next()
                     .getComponents()
                     .add(ModelFactory.eINSTANCE.createRouter());
               });
    Assertions.assertTrue(
        assertView(
            getDefaultView(vsum, List.of(System.class, Root.class)),
            (View v) -> {
          // assert that the router has been added and that the corresponding entity has
          // been created
              return v.getRootObjects(System.class)
                  .iterator()
                  .next()
                  .getComponents()
                  .get(0)
                  .getName()
                  .equals(
                      v.getRootObjects(Root.class)
                          .iterator()
                          .next()
                          .getEntities()
                          .get(0)
                          .getName());
        }));
  }

  @Test
  void renameComponent(@TempDir Path tempDir) throws IOException {
    final String newName = "newName";
    VirtualModel vsum = createDefaultVirtualModel(tempDir);
    addSystem(vsum, tempDir);
    addComponent(vsum);
    modifyView(
        getDefaultView(vsum, List.of(System.class)).withChangeDerivingTrait(),
        (CommittableView v) -> {
                 // change the name of the component
                 v.getRootObjects(System.class)
                     .iterator()
                     .next()
                     .getComponents()
                     .get(0)
                     .setName(newName);
               });
    Assertions.assertTrue(
        assertView(
            getDefaultView(vsum, List.of(System.class, Root.class)),
            (View v) -> {
          // assert that the renaming worked on the component as well as the corresponding
          // entity
              return v.getRootObjects(System.class)
                      .iterator()
                      .next()
                      .getComponents()
                      .get(0)
                      .getName()
                      .equals(newName)
                  && v.getRootObjects(Root.class)
                      .iterator()
                      .next()
                      .getEntities()
                      .get(0)
                      .getName()
                      .equals(newName);
        }));
  }

  @Test
  void deleteComponent(@TempDir Path tempDir) throws IOException {
    VirtualModel vsum = createDefaultVirtualModel(tempDir);
    addSystem(vsum, tempDir);
    addComponent(vsum);
    modifyView(
        getDefaultView(vsum, List.of(System.class)).withChangeDerivingTrait(),
        (CommittableView v) -> {
                 v.getRootObjects(System.class).iterator().next().getComponents().remove(0);
               });
    Assertions.assertTrue(
        assertView(
            getDefaultView(vsum, List.of(System.class, Root.class)),
            (View v) -> {
          // assert that the deletion of the component worked and that the corresponding
          // entity also got deleted
          return v.getRootObjects(System.class).iterator().next().getComponents().isEmpty()
                     && v.getRootObjects(Root.class).iterator().next().getEntities().isEmpty();
        }));
  }

  @Test
  void testLink(@TempDir Path tempDir) throws IOException {
    VirtualModel vsum = createDefaultVirtualModel(tempDir);
    addSystem(vsum, tempDir);

    // add two components, protocol and add a link between them
    modifyView(
        getDefaultView(vsum, List.of(System.class)).withChangeDerivingTrait(),
        (CommittableView v) -> {
                 var system = v.getRootObjects(System.class).iterator().next();

                 var component1 = ModelFactory.eINSTANCE.createComponent();
                 component1.setName("component1");
                 var component2 = ModelFactory.eINSTANCE.createComponent();
                 component2.setName("component2");
                 system.getComponents().addAll(List.of(component1, component2));

                 var protocol = ModelFactory.eINSTANCE.createProtocol();
                 protocol.setName("exampleProtocol");
                 system.getProtocols().add(protocol);

                 var link = ModelFactory.eINSTANCE.createLink();
                 system.getLinks().add(link);
                 link.setProtocol(protocol);
                 link.getComponents().addAll(List.of(component1, component2));
               });

    // assert that the link has been created and that it is connected to the two
    // components
    Assertions.assertTrue(
        assertView(
            getDefaultView(vsum, List.of(Root.class)),
            (View v) -> {
              var root = v.getRootObjects(Root.class).iterator().next();
      return root.getLinks().size() == 1 && root.getLinks().get(0).getEntities().size() == 2
                 && root.getLinks()
                        .get(0)
                        .getEntities()
                        .stream()
                        .allMatch(c -> c.getName().startsWith("component"));
    }));
  }

  /**
   * Demonstrates manual constraint evaluation using VitruviusOCL.
   *
   * <p>After inserting a Component and committing the change, the Reactions create a corresponding
   * Entity in model2. The OCL constraints in {@code constraints.ocl} are then evaluated manually
   * to verify that the VSUM is in a consistent state.
   */
  @Test
  void constraintsAreSatisfiedAfterComponentInsert(@TempDir Path tempDir) throws IOException {
    InternalVirtualModel vsum = createDefaultVirtualModel(tempDir);
    addSystem(vsum, tempDir);
    addComponent(vsum);

    // Register the VSUM so VitruviusOCL can access model instances and correspondences.
    VitruvOCL.registerVSUM(vsum);

    var result = VitruvOCL.evaluateConstraints(CONSTRAINT_FILE);
    Assertions.assertTrue(
        result.allSatisfied(),
        () -> {
          var problems = new java.util.ArrayList<>(result.getViolatedConstraints());
          problems.addAll(result.getFailedConstraints());
          return "Expected all OCL constraints to be satisfied after component insertion ("
              + result.getSummary()
              + "):\n"
              + problems.stream().map(Object::toString).collect(java.util.stream.Collectors.joining("\n"))
              + "\n";
        });
  }
}
