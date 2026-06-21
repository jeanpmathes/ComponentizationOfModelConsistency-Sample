package tools.vitruv.methodologisttemplate.viewtype;

import org.eclipse.emf.ecore.resource.Resource;
import tools.vitruv.change.atomic.hid.HierarchicalId;
import tools.vitruv.change.atomic.uuid.Uuid;
import tools.vitruv.change.composite.description.VitruviusChange;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.change.correspondence.view.CorrespondenceModelView;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.compmodelcons.views.impl.TransformingViewType;
import tools.vitruv.methodologisttemplate.model.model2.Model2Factory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ModelAsModel2ViewType extends TransformingViewType { // todo: remove when generated
    public ModelAsModel2ViewType(String name) {
        super(name, Model2Factory.eINSTANCE.getModel2Package());
    }

    @Override
    protected void generateView(Collection<Resource> sources, Optional<CorrespondenceModelView<? extends Correspondence>> correspondenceModel, List<Resource> target) {
    }

    @Override
    protected VitruviusChange<Uuid> transformChange(Collection<Resource> sources, Optional<EditableCorrespondenceModelView<? extends Correspondence>> correspondenceModel, VitruviusChange<HierarchicalId> change) {
        return null;
    }
}
