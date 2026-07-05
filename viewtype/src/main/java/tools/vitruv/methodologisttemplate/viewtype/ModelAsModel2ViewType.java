package tools.vitruv.methodologisttemplate.viewtype;

import org.eclipse.emf.ecore.EPackage;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.compmodelcons.change.ChangeSpecificationAwareViewType;
import tools.vitruv.compmodelcons.views.operations.Operation;
import tools.vitruv.methodologisttemplate.model.model.ModelPackage;
import tools.vitruv.methodologisttemplate.model.model2.Model2Package;

import java.util.List;

public class ModelAsModel2ViewType extends ChangeSpecificationAwareViewType { // todo: remove when generated
    private static final EPackage metamodel = Model2Package.eINSTANCE;

    public ModelAsModel2ViewType(String name) {
        super(name, metamodel);
    }

    @Override
    public List<MetamodelDescriptor> getOriginMetamodelDescriptors() {
        return List.of(MetamodelDescriptor.of(ModelPackage.eINSTANCE));
    }

    @Override
    public MetamodelDescriptor getViewTypeMetamodelDescriptor() {
        return MetamodelDescriptor.of(metamodel);
    }

    @Override
    protected Operation createStructure() {
        return null;
    }
}
