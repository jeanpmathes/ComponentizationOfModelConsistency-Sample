package tools.vitruv.methodologisttemplate.viewtype;

import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.compmodelcons.change.AbstractViewChangePropagationParticipationSpecification;
import tools.vitruv.compmodelcons.change.ViewChangePropagationParticipationSpecification;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.methodologisttemplate.model.model.ModelPackage;
import tools.vitruv.methodologisttemplate.model.model.System;

import java.util.Collection;
import java.util.List;

// todo: remove when generated
public class ModelAsModelViewTypeChangePropagationParticipationSpecification extends AbstractViewChangePropagationParticipationSpecification {

    @Override
    public MetamodelDescriptor getOriginMetamodelDescriptor() {
        return MetamodelDescriptor.of(ModelPackage.eINSTANCE);
    }

    @Override
    public MetamodelDescriptor getViewTypeMetamodelDescriptor() {
        return MetamodelDescriptor.of(ModelPackage.eINSTANCE);
    }

    @Override
    public Collection<Class<?>> getRootTypes() {
        return List.of(System.class);
    }
}
