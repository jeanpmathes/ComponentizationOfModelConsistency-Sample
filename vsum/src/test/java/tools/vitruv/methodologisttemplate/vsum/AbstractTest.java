package tools.vitruv.methodologisttemplate.vsum;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import mir.reactions.modelView2ModelView2.ModelView2ModelView2ChangePropagationSpecification;
import neojoin.viewtypes.example.ExampleViewType;
import neojoin.viewtypes.model2_identity.Model2IdentityViewType;
import neojoin.viewtypes.model_identity.ModelIdentityViewType;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.BeforeAll;
import tools.vitruv.change.propagation.ChangePropagationMode;
import tools.vitruv.change.testutils.TestUserInteraction;
import tools.vitruv.compmodelcons.change.ViewChangePropagationSpecificationAdapterFactory;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.views.ViewType;
import tools.vitruv.framework.views.ViewTypeFactory;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.framework.vsum.VirtualModelBuilder;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;
import tools.vitruv.methodologisttemplate.model.model.ModelFactory;
import tools.vitruv.methodologisttemplate.model.model.System;

public class AbstractTest {
  @BeforeAll
  static void setup() {
    Resource.Factory.Registry.INSTANCE
        .getExtensionToFactoryMap()
        .put("*", new XMIResourceFactoryImpl());
  }

  protected void addSystem(VirtualModel vsum, Path projectPath) {
    CommittableView view = getDefaultView(vsum, List.of(
        tools.vitruv.methodologisttemplate.model.model.System.class)).withChangeDerivingTrait();
    modifyView(view, (CommittableView v) -> {
      v.registerRoot(
          ModelFactory.eINSTANCE.createSystem(),
          URI.createFileURI(projectPath.toString() + "/example.model"));
    });
  }

  // See https://github.com/vitruv-tools/Vitruv/issues/717 for more information
  // about the rootTypes
  protected View getDefaultView(VirtualModel vsum, Collection<Class<?>> rootTypes) {
    var selector = vsum.createSelector(ViewTypeFactory.createIdentityMappingViewType("default"));
    selector
        .getSelectableElements()
        .stream()
        .filter(element -> rootTypes
                               .stream()
                               .anyMatch(it -> it.isInstance(element)))
        .forEach(it -> selector.setSelected(it, true));
    return selector.createView();
  }

  // These functions are only for convience, as they make the code a bit better
  // readable
  protected void modifyView(CommittableView view, Consumer<CommittableView> modificationFunction) {
    modificationFunction.accept(view);
    view.commitChanges();
  }

  protected void addComponent(VirtualModel vsum) {
    addComponent(vsum, "specialname");
  }

  protected void addComponent(VirtualModel vsum, String name) {
    CommittableView view = getDefaultView(vsum, List.of(
        tools.vitruv.methodologisttemplate.model.model.System.class)).withChangeDerivingTrait();
    modifyView(view, (CommittableView v) -> {
      var component = ModelFactory.eINSTANCE.createComponent();
      component.setName(name);
      v
          .getRootObjects(tools.vitruv.methodologisttemplate.model.model.System.class)
          .iterator()
          .next()
          .getComponents()
          .add(component);
    });
  }

  protected void addRouter(VirtualModel vsum) {
    addRouter(vsum, "specialRouterName");
  }

  protected void addRouter(VirtualModel vsum, String name) {
    CommittableView view = getDefaultView(vsum, List.of(
        tools.vitruv.methodologisttemplate.model.model.System.class)).withChangeDerivingTrait();
    modifyView(view, (CommittableView v) -> {
      var component = ModelFactory.eINSTANCE.createRouter();
      component.setName(name);
      v
          .getRootObjects(System.class)
          .iterator()
          .next()
          .getComponents()
          .add(component);
    });
  }

  protected InternalVirtualModel createDefaultVirtualModel(Path projectPath) throws IOException {
    return createDefaultVirtualModel(projectPath, List.of());
  }

  protected InternalVirtualModel createDefaultVirtualModel(
      Path projectPath,
      List<ViewType<?>> viewTypes)
  throws IOException {
    InternalVirtualModel model = new VirtualModelBuilder()
                                     .withStorageFolder(projectPath)
                                     .withUserInteractorForResultProvider(
                                         new TestUserInteraction.ResultProvider(
                                             new TestUserInteraction()))
                                     .withChangePropagationSpecifications(
                                         ViewChangePropagationSpecificationAdapterFactory.INSTANCE.create(
                                             Optional.of(new ModelIdentityViewType(null)),
                                             new ModelView2ModelView2ChangePropagationSpecification(),
                                             Optional.of(new Model2IdentityViewType(null))))
                                     .withViewTypes(viewTypes)
                                     .withViewType(ExampleViewType::new)
                                     .buildAndInitialize();
    model.setChangePropagationMode(ChangePropagationMode.TRANSITIVE_CYCLIC);
    return model;
  }

  protected boolean assertView(View view, Function<View, Boolean> viewAssertionFunction) {
    return viewAssertionFunction.apply(view);
  }
}
