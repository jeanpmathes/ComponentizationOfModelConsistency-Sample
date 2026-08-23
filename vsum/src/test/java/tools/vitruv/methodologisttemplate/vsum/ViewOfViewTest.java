package tools.vitruv.methodologisttemplate.vsum;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import mir.reactions.model2Model2.Model2Model2ChangePropagationSpecification;
import mir.reactions.vov_modelView2ModelView2.Vov_modelView2ModelView2ChangePropagationSpecification;
import neojoin.viewtypes.model2_identity.Model2IdentityViewType;
import neojoin.viewtypes.model_identity.ModelIdentityViewType;
import neojoin.viewtypes.view_of_view.Root;
import neojoin.viewtypes.view_of_view.ViewOfViewViewType;
import neojoin.viewtypes.vov_model2_identity.VovModel2IdentityViewType;
import neojoin.viewtypes.vov_model_identity.VovModelIdentityViewType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.change.propagation.ChangePropagationMode;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.change.testutils.TestUserInteraction;
import tools.vitruv.compmodelcons.change.CorrespondenceTranslation;
import tools.vitruv.compmodelcons.change.ViewChangePropagationSpecificationAdapterFactory;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.framework.vsum.VirtualModelBuilder;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;
import tools.vitruv.methodologisttemplate.model.model.ModelFactory;
import tools.vitruv.methodologisttemplate.model.model.System;

public class ViewOfViewTest extends AbstractTest {
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

  @Test
  void testEdit(@TempDir Path tempDir) throws IOException {
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

    modifyView(getView(vsum).withChangeDerivingTrait(), (CommittableView v) -> {
      Root root = v
          .getRootObjects(Root.class)
          .iterator()
          .next();

      root
          .getAllThings()
          .getLast()
          .setName("ComponentRenamed");
    });

    Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(System.class)), (View v) -> {
      var system = v
          .getRootObjects(System.class)
          .iterator()
          .next();

      var component = system
          .getComponents()
          .stream()
          .filter(c -> c
              .getName()
              .equals("ComponentRenamed"))
          .findAny()
          .orElseThrow();

      return component
          .getName()
          .equals("ComponentRenamed");
    }));
  }

  @Test
  void testPropagateSystem(@TempDir Path tempDir) throws IOException {
    VirtualModel vsum = createVirtualModelWithViewOfViewBasedConsistency(tempDir);

    addSystem(vsum, tempDir);

    Assertions.assertTrue(assertView(getView(vsum), (View v) -> {
      Root root = v
          .getRootObjects(Root.class)
          .iterator()
          .next();

      return root
          .getAllThings()
          .size() == 0;
    }));
  }

  private VirtualModel createVirtualModel(Path tempDir) throws IOException {
    InternalVirtualModel model = new VirtualModelBuilder()
        .withStorageFolder(tempDir)
        .withUserInteractorForResultProvider(
            new TestUserInteraction.ResultProvider(new TestUserInteraction()))
        .withChangePropagationSpecifications(new Model2Model2ChangePropagationSpecification())
        .withViewTypes(List.of(new ModelIdentityViewType(null), new Model2IdentityViewType(null)))
        .withViewType(ViewOfViewViewType::new)
        .buildAndInitialize();
    model.setChangePropagationMode(ChangePropagationMode.TRANSITIVE_CYCLIC);
    return model;
  }

  private VirtualModel createVirtualModelWithViewOfViewBasedConsistency(Path tempDir)
  throws IOException {
    ChangePropagationSpecification inner =
        ViewChangePropagationSpecificationAdapterFactory.INSTANCE.create(
            Optional.of(new VovModelIdentityViewType(null)),
            new Vov_modelView2ModelView2ChangePropagationSpecification(),
            Optional.of(new VovModel2IdentityViewType(null)));

    ChangePropagationSpecification outer =
        ViewChangePropagationSpecificationAdapterFactory.INSTANCE.create(
            Optional.of(new ModelIdentityViewType(null)),
            inner,
            Optional.of(new Model2IdentityViewType(null)),
            CorrespondenceTranslation.NONE);

    InternalVirtualModel model = new VirtualModelBuilder()
        .withStorageFolder(tempDir)
        .withUserInteractorForResultProvider(
            new TestUserInteraction.ResultProvider(new TestUserInteraction()))
        .withChangePropagationSpecifications(outer)
        .withViewTypes(List.of(new ModelIdentityViewType(null), new Model2IdentityViewType(null)))
        .withViewType(ViewOfViewViewType::new)
        .buildAndInitialize();
    model.setChangePropagationMode(ChangePropagationMode.TRANSITIVE_CYCLIC);
    return model;
  }

  private View getView(VirtualModel vsum) {
    return vsum
        .createSelector(vsum
                            .getViewTypes()
                            .stream()
                            .filter(viewType -> viewType
                                .getName()
                                .equals(ViewOfViewViewType.NAME))
                            .findAny()
                            .orElseThrow())
        .createView();
  }
}
