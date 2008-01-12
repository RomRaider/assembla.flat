package enginuity.ramtune;

import java.util.HashMap;
import java.util.Set;

import enginuity.io.ds.DataSource;
import enginuity.newmaps.ecumetadata.ImageFragmentDefinition;
import enginuity.newmaps.ecumetadata.RomMetadata;
import enginuity.newmaps.ecumetadata.TableMetadata;
import enginuity.newmaps.ecumetadata.feature.RamTuneFeatureDefinition;
import enginuity.util.NamedSet;

public class RamTuneFeature {

	private static volatile HashMap<String, TableMetadata> ramTableDefs;
	
	private Boolean isFeatureInstalled;
	
	private Boolean isActive;

	private DataSource source;
	
	private RomMetadata romDefinition;
	
	private RamTuneFeatureDefinition definition;


	public RamTuneFeature(DataSource source, RomMetadata romDefinition) {
		this.source = source;
		this.romDefinition = romDefinition;
		definition = (RamTuneFeatureDefinition)
			romDefinition.getFeatures().get("RamTune");
	}

	public synchronized boolean isFeatureInstalled() throws Exception {
		if (isFeatureInstalled == null) {
			boolean _isFeatureInstalled = true;

			try {
				source.open();
				for (ImageFragmentDefinition fragDef :
					definition.getInstallFragments()) {

					byte[] sourceData = source.readRom(
						fragDef.getOffset(), fragDef.getData().length);

					if (!sourceData.equals(fragDef.getData())) {
						_isFeatureInstalled = false;
						break;
					}
				}
			} finally {
				source.close();
			}
			
			isFeatureInstalled = new Boolean(_isFeatureInstalled);
		}
		return isFeatureInstalled.booleanValue();
	}
	
	public synchronized void toggleFeatureInstallation(boolean install)
		throws Exception {

		if (isFeatureInstalled() != install) {

			try {
				Set<ImageFragmentDefinition> fragments;
				
				if (install)
					fragments = definition.getInstallFragments();
				else
					fragments = definition.getUninstallFragments();
				
				source.open();
				for (ImageFragmentDefinition fragDef : fragments)
					source.writeRom(fragDef.getOffset(), fragDef.getData());

			} finally {
				source.close();
			}
			isFeatureInstalled = new Boolean(install);
		}
	}
	
	public synchronized boolean isActive() throws Exception {
		if (isActive == null) {
			boolean _isActive = true;

			byte control =
				source.readRam(definition.getControlByteOffset(), 1)[0];
			_isActive = control >> 7 > 0;
			
			isActive = new Boolean(_isActive);
		}
		return isActive.booleanValue();
	}
	
	public synchronized void activate() throws Exception {
		toggleActive(true, true);
	}
	
	public synchronized void inactivate(boolean persistChanges)
		throws Exception {

		toggleActive(false, persistChanges);
	}
	
	private void toggleActive(boolean active, boolean copy) throws Exception {
		if (isActive() != active) {
			
			try {
				source.open();

				byte control =
					source.readRam(definition.getControlByteOffset(), 1)[0];

				// TODO: Determine if the following is correct and use the
				// appropriate libraries in the enginuity.util package

				// Set the first bit that indicates RamTune is
				// using tables in RAM
				control = (byte) (control | 128);

				source.writeRam(definition.getControlByteOffset(),
					new byte[] {control} );
					
				if (copy)
					copyTables(active);
			} finally {
				source.close();
			}

			isActive = new Boolean(active);
		}
	}
	
	/**
	 * Copies tables between RAM and ROM
	 * @param copyDirection If <code>true</code>, copies tables from ROM to RAM,
	 * otherwise from RAM to ROM.
	 * @throws Exception
	 */
	private void copyTables(boolean copyDirection) throws Exception {

		// Collect the RamTune table definitions
		HashMap<String, TableMetadata> tables = getRamTuneTableDefinitions();
		
		// Byte array copy using the romOffset, ramOffset, and length
		try {
			source.open();
			for (TableMetadata table : tables.values()) {
				byte[] data;
				if (copyDirection) {
					data = source.readRom(table.getAddress(),
						table.getImageSize());
					source.writeRam(table.getRamAddress(), data);
				} else {
					data = source.readRam(table.getAddress(),
						table.getImageSize());
					source.writeRom(table.getRamAddress(), data);
				}
			}
		} finally {
			source.close();
		}
	}
	
	private HashMap<String, TableMetadata> getRamTuneTableDefinitions() {

		if (ramTableDefs == null) {
			ramTableDefs = new HashMap<String, TableMetadata>();
			NamedSet<TableMetadata> tables = romDefinition.getTables();
			for (TableMetadata table : tables)
				if (table.getRamAddress() != 0)
					ramTableDefs.put(table.getName(), table);
		}
		return ramTableDefs;
	}
}
