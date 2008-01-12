package enginuity.newmaps.ecumetadata.feature;

import java.util.Set;

import enginuity.newmaps.ecumetadata.ImageFragmentDefinition;

public interface FeatureDefinition {

	public int getName();
	
	public int getDescription();
	
	public Set<ImageFragmentDefinition> getInstallFragments();
	
	public Set<ImageFragmentDefinition> getUninstallFragments();
}
