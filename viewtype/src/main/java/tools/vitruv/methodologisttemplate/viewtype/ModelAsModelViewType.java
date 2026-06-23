package tools.vitruv.methodologisttemplate.viewtype;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import tools.vitruv.change.atomic.hid.HierarchicalId;
import tools.vitruv.change.atomic.uuid.Uuid;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.change.composite.description.VitruviusChange;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.change.correspondence.view.CorrespondenceModelView;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.compmodelcons.views.impl.ChangeSpecificationAwareViewType;
import tools.vitruv.methodologisttemplate.model.model.ModelPackage;
import tools.vitruv.methodologisttemplate.model.model2.Model2Package;
import tools.vitruv.methodologisttemplate.model.model2.Root;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ModelAsModelViewType extends ChangeSpecificationAwareViewType { // todo: remove when generated
    private static final EPackage metamodel = ModelPackage.eINSTANCE;

    public ModelAsModelViewType(String name) {
        super(name, metamodel);
    }

    @Override
    protected void generateView(Collection<Resource> sources, Optional<CorrespondenceModelView<? extends Correspondence>> correspondenceModel, List<Resource> target) {
    }

    @Override
    protected VitruviusChange<Uuid> transformChange(Collection<Resource> sources, Optional<EditableCorrespondenceModelView<? extends Correspondence>> correspondenceModel, VitruviusChange<HierarchicalId> change) {
        return null;
    }

    @Override
    protected Collection<Class<?>> getRootTypes() {
        return List.of(Root.class);
    }

    @Override
    public MetamodelDescriptor getOriginMetamodelDescriptor() {
        return MetamodelDescriptor.of(ModelPackage.eINSTANCE);
    }

    @Override
    public MetamodelDescriptor getViewTypeMetamodelDescriptor() {
        return MetamodelDescriptor.of(metamodel);
    }
}
