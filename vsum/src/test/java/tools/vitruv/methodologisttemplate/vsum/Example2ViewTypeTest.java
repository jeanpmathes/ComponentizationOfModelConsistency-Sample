package tools.vitruv.methodologisttemplate.vsum;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import neojoin.viewtypes.example2.Example2ViewType;
import neojoin.viewtypes.example2.Root;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.methodologisttemplate.model.model.ModelFactory;
import tools.vitruv.methodologisttemplate.model.model.System;

public class Example2ViewTypeTest extends AbstractTest {
  @Test
  void testJoinAndWhere(@TempDir Path tempDir) throws IOException {
    VirtualModel vsum = createVirtualModel(tempDir);
    addSystem(vsum, tempDir);

    modifyView(getDefaultView(vsum, List.of(System.class)).withChangeRecordingTrait(),
               (CommittableView v) -> {
                 var system = v
                     .getRootObjects(System.class)
                     .iterator()
                     .next();

                 var component1 = ModelFactory.eINSTANCE.createComponent();
                 component1.setName("Component1");
                 system
                     .getComponents()
                     .add(component1);

                 var component2 = ModelFactory.eINSTANCE.createComponent();
                 component2.setName("Component2");
                 system
                     .getComponents()
                     .add(component2);

                 var component3 = ModelFactory.eINSTANCE.createComponent();
                 component3.setName("Component3");
                 system
                     .getComponents()
                     .add(component3);
               });

    Assertions.assertTrue(assertView(getView(vsum), (View v) -> {
      Root root = v
          .getRootObjects(Root.class)
          .iterator()
          .next();

      root
          .getAllThings()
          .stream()
          .filter(t -> t
              .getName()
              .equals("Component1"))
          .findAny()
          .orElseThrow();
      root
          .getAllThings()
          .stream()
          .filter(t -> t
              .getName()
              .equals("Component2"))
          .findAny()
          .orElseThrow();
      root
          .getAllThings()
          .stream()
          .filter(t -> t
              .getName()
              .equals("Component3"))
          .findAny()
          .orElseThrow();

      return root
          .getAllThings()
          .size() == 3;
    }));
  }

  private VirtualModel createVirtualModel(Path tempDir) throws IOException {
    return createDefaultVirtualModel(tempDir, List.of(new Example2ViewType()));
  }

  private View getView(VirtualModel vsum) {
    return vsum
        .createSelector(vsum
                            .getViewTypes()
                            .stream()
                            .filter(viewType -> viewType
                                .getName()
                                .equals(Example2ViewType.NAME))
                            .findAny()
                            .orElseThrow())
        .createView();
  }
}
