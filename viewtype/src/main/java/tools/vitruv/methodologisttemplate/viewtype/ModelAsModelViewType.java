package tools.vitruv.methodologisttemplate.viewtype;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.compmodelcons.change.ChangeSpecificationAwareViewType;
import tools.vitruv.compmodelcons.views.operations.Operation;
import tools.vitruv.methodologisttemplate.model.model.ModelPackage;
import tools.vitruv.methodologisttemplate.model.model2.Root;

import java.util.Collection;
import java.util.List;

public class ModelAsModelViewType extends ChangeSpecificationAwareViewType { // todo: remove when generated
    private static final EPackage metamodel = ModelPackage.eINSTANCE;

    public ModelAsModelViewType(String name) {
        super(name, metamodel);
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

    @Override
    protected Operation createStructure(Resource model) {
        return null;
    }
}
