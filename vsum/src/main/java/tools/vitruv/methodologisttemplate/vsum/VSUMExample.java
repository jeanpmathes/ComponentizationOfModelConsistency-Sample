package tools.vitruv.methodologisttemplate.vsum;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import mir.reactions.modelView2ModelView2.ModelView2ModelView2ChangePropagationSpecification;
import neojoin.viewtypes.example.ExampleViewType;
import neojoin.viewtypes.model2_identity.Model2IdentityViewType;
import neojoin.viewtypes.model_identity.ModelIdentityViewType;
import tools.vitruv.change.testutils.TestUserInteraction;
import tools.vitruv.compmodelcons.change.ViewChangePropagationSpecificationAdapterFactory;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.views.ViewTypeFactory;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.framework.vsum.VirtualModelBuilder;

/**
 * This class provides an example how to define and use a VSUM.
 */
public class VSUMExample {
  // By default, this is zero. In the case of a componentized VSUM, we would need to select the
  // correct level. Say we have two parts, one with models A and B, and one with models C and D.
  // Then, the specifications A<->B and C<->D would be on level 0,
  // while the specification A<->C would be on level 1.
  // This ensures that a part is fully consistent before we propagate changes to the other part.
  private static final int CHANGE_PROPAGATION_SPECIFICATION_LEVEL = 0;

  public static void main(String[] args) throws IOException {
    VirtualModel vsum = createDefaultVirtualModel();
    CommittableView view = getDefaultView(vsum).withChangeDerivingTrait();
    modifyView(view, (CommittableView v) -> {

    });
  }

  private static VirtualModel createDefaultVirtualModel() throws IOException {
    return new VirtualModelBuilder().withStorageFolder(Path.of("vsumexample"))
               .withUserInteractorForResultProvider(
                   new TestUserInteraction.ResultProvider(new TestUserInteraction()))
               .withChangePropagationSpecifications(CHANGE_PROPAGATION_SPECIFICATION_LEVEL,
                   ViewChangePropagationSpecificationAdapterFactory.INSTANCE.create(
                       Optional.of(new ModelIdentityViewType(null)),
                       new ModelView2ModelView2ChangePropagationSpecification(),
                       Optional.of(new Model2IdentityViewType(null))))
               .withViewType(ExampleViewType::new)
               .buildAndInitialize();
  }

  private static View getDefaultView(VirtualModel vsum) {
    var selector = vsum.createSelector(ViewTypeFactory.createIdentityMappingViewType("default"));
    selector.getSelectableElements().forEach(it -> selector.setSelected(it, true));
    return selector.createView();
  }

  private static void modifyView(
      CommittableView view,
      Consumer<CommittableView> modificationFunction) {
    modificationFunction.accept(view);
    view.commitChanges();
  }
}
