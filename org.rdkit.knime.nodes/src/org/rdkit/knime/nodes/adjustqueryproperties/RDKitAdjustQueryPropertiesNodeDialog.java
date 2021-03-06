/*
 * ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright (C)2016
 * Novartis Institutes for BioMedical Research
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 */
package org.rdkit.knime.nodes.adjustqueryproperties;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.RDKit.AdjustQueryParameters;
import org.RDKit.AdjustQueryWhichFlags;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.rdkit.knime.nodes.AbstractRDKitNodeSettingsPane;
import org.rdkit.knime.types.RDKitMolValue;
import org.rdkit.knime.util.DialogComponentColumnNameSelection;
import org.rdkit.knime.util.DialogComponentEnumFilterPanel;
import org.rdkit.knime.util.DialogComponentSeparator;
import org.rdkit.knime.util.SettingsModelEnumerationArray;

/**
 * <code>NodeDialog</code> for the "RDKitAdjustQueryProperties" Node.
 * Structure searches based on queries molecules are not always leading to the desired results. 
 * Often some fine tuning of the query structure helps to increase the search results. 
 * RDKit offers query properties that can be set explicitly for query molecules to influence a search. 
 * This node lets the user define the properties to be adjusted.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Manuel Schwarze
 */
public class RDKitAdjustQueryPropertiesNodeDialog extends AbstractRDKitNodeSettingsPane {

	//
	// Constants
	//
	
	/** The reference default parameters taken directly from RDKit to define our own default here. */
	public final static AdjustQueryParameters DEFAULT_ADJUST_QUERY_PARAMETERS = new AdjustQueryParameters();
	
	public final static DefaultListCellRenderer TOOLTIP_LIST_RENDERER = new DefaultListCellRenderer() {

		//
		// Constants
		//

		/** Serial number. */
		private static final long serialVersionUID = -3432112665221820183L;

		//
		// Public Methods
		//

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Component getListCellRendererComponent(
				final JList<?> list, final Object value, final int index,
				final boolean isSelected, final boolean cellHasFocus) {
			// The super method will reset the icon if we call this method
			// last. So we let super do its job first and then we take care
			// that everything is properly set.
			final Component c =  super.getListCellRendererComponent(list, value, index,
					isSelected, cellHasFocus);

			assert (c == this);

			if (value instanceof AdjustQueryWhichFlags) {
				final AdjustQueryWhichFlags flag = (AdjustQueryWhichFlags)value;

				// Set text
				setText(flag.toString());

				// Set tooltip
				final String strTooltip = "<html><body><b>" + flag.name() + "</b> - 0x" +
						Integer.toHexString(flag.swigValue()) + "</body></html>";

				list.setToolTipText(strTooltip);
			}

			return this;
		}
	};
    
	
	//
	// Constructor
	//
	
    /**
     * Create a new dialog pane with default components to configure an input column,
     * the name of a new column, which will contain the calculation results, an option
     * to tell, if the source column shall be removed from the result table.
     */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	RDKitAdjustQueryPropertiesNodeDialog() {
        super.addDialogComponent(new DialogComponentColumnNameSelection(
                createInputColumnNameModel(), "RDKit Mol column: ", 0,
                RDKitMolValue.class));
        super.addDialogComponent(new DialogComponentString(
                createNewColumnNameModel(), "New column name: ", true, 40));
        super.addDialogComponent(new DialogComponentBoolean(
                createRemoveSourceColumnsOptionModel(), "Remove source column"));
        
        super.addDialogComponent(new DialogComponentSeparator());

        SettingsModelBoolean adjustDegreeModel = createAdjustDegreeOptionModel();
        super.addDialogComponent(new DialogComponentBoolean(
        		adjustDegreeModel, "Adjust degree"));
        DialogComponentEnumFilterPanel<AdjustQueryWhichFlags> filterPanelDegreeOption = 
        		new DialogComponentEnumFilterPanel(
                		createAdjustDegreeFlagsOptionModel(adjustDegreeModel), 
                		"Use the following flags to adjust degree:",
                		generateReducedFlagList(), true) {
		        	protected void setEnabledComponents(boolean enabled) {
		        		super.setEnabledComponents(enabled);
		        		setListCellRenderer(enabled ? TOOLTIP_LIST_RENDERER : new DefaultListCellRenderer());
		        	}
		        };
        super.addDialogComponent(filterPanelDegreeOption);
        filterPanelDegreeOption.setSearchVisible(false);
        filterPanelDegreeOption.setExcludeTitle(" Do not use ");
        filterPanelDegreeOption.setIncludeTitle(" Use ");
        filterPanelDegreeOption.setListCellRenderer(TOOLTIP_LIST_RENDERER);
        filterPanelDegreeOption.getComponentPanel().setPreferredSize(new Dimension(550, 135));
        super.addDialogComponent(new DialogComponentSeparator());
        
        SettingsModelBoolean adjustRingCountModel = createAdjustRingCountOptionModel();
        super.addDialogComponent(new DialogComponentBoolean(
        		adjustRingCountModel, "Adjust ring count"));
        DialogComponentEnumFilterPanel<AdjustQueryWhichFlags> filterPanelRingCountOption = 
        		new DialogComponentEnumFilterPanel(
                		createAdjustRingCountFlagsOptionModel(adjustRingCountModel), 
                		"Use the following flags to adjust ring count:", 
                		generateReducedFlagList(), true) {
                	protected void setEnabledComponents(boolean enabled) {
                		super.setEnabledComponents(enabled);
                		setListCellRenderer(enabled ? TOOLTIP_LIST_RENDERER :  new DefaultListCellRenderer());
                	}
                };
        super.addDialogComponent(filterPanelRingCountOption);
        filterPanelRingCountOption.setSearchVisible(false);
        filterPanelRingCountOption.setExcludeTitle(" Do not use ");
        filterPanelRingCountOption.setIncludeTitle(" Use ");
        filterPanelRingCountOption.setListCellRenderer(TOOLTIP_LIST_RENDERER);
        filterPanelRingCountOption.getComponentPanel().setPreferredSize(new Dimension(550, 135));
   
        super.addDialogComponent(new DialogComponentSeparator());
        
        super.addDialogComponent(new DialogComponentBoolean(
        		createMakeDummiesQueriesOptionModel(), "Make dummies queries"));
    }

    //
    // Static Methods
    //
    
    /**
     * Creates the settings model to be used for the input column.
     * 
     * @return Settings model for input column selection.
     */
    static final SettingsModelString createInputColumnNameModel() {
        return new SettingsModelString("input_column", null);
    }

    /**
     * Creates the settings model to be used to specify the new column name.
     * 
     * @return Settings model for result column name.
     */
    static final SettingsModelString createNewColumnNameModel() {
        return new SettingsModelString("new_column_name", null);
    }

    /**
     * Creates the settings model for the boolean flag to determine, if
     * the source column shall be removed from the result table.
     * The default is false.
     * 
     * @return Settings model for check box whether to remove source columns.
     */
    static final SettingsModelBoolean createRemoveSourceColumnsOptionModel() {
        return new SettingsModelBoolean("remove_source_columns", false);
    }
    
    /**
     * Creates the settings model to be used to specify, if degree shall be adjusted.
     * 
     * @return Settings model for adjusting degree.
     */
    static final SettingsModelBoolean createAdjustDegreeOptionModel() {
        return new SettingsModelBoolean("adjust_degree", 
        		DEFAULT_ADJUST_QUERY_PARAMETERS.getAdjustDegree());
    }
    
    /**
     * Creates the settings model to be used to specify, if ring count shall be adjusted.
     * 
     * @return Settings model for adjusting ring count.
     */
    static final SettingsModelBoolean createAdjustRingCountOptionModel() {
        return new SettingsModelBoolean("adjust_ring_count", 
        		DEFAULT_ADJUST_QUERY_PARAMETERS.getAdjustRingCount());
    }
    
    /**
     * Creates the settings model to be used to specify, if dummies queries shall be made.
     * 
     * @return Settings model for making dummies queries.
     */
    static final SettingsModelBoolean createMakeDummiesQueriesOptionModel() {
        return new SettingsModelBoolean("make_dummies_queries", 
        		DEFAULT_ADJUST_QUERY_PARAMETERS.getMakeDummiesQueries());
    }
    
    /**
     * Removes elements from the {@link AdjustQueryWhichFlags} enumeration, which 
     * are not meaningful in the UI.
     * @return
     */
    static final List<AdjustQueryWhichFlags> generateReducedFlagList() {
    	ArrayList<AdjustQueryWhichFlags> list = new ArrayList<>(Arrays.asList(AdjustQueryWhichFlags.values()));
    	list.remove(AdjustQueryWhichFlags.ADJUST_IGNORENONE);
    	list.remove(AdjustQueryWhichFlags.ADJUST_IGNOREALL);
    	return list;
    }
    
    /**
     * Creates the settings model to be used to specify, how degrees shall be adjusted,
     * which has only an effect, if the adjust degree option is enabled.
     * 
     * @param adjustDegreeModel Model that the model to be created depends on.
     * 	Based on the state of the passed in model we will enable or disable the
     *  AdjustDegreeFlags model.
     * 
     * @return Settings model for defining degree adjustment.
     */
    static final SettingsModelEnumerationArray<AdjustQueryWhichFlags> createAdjustDegreeFlagsOptionModel(
    		final SettingsModelBoolean adjustDegreeModel) {
    	SettingsModelEnumerationArray<AdjustQueryWhichFlags> model = 
    			new SettingsModelEnumerationArray<>(AdjustQueryWhichFlags.class, "adjust_degree_flags", 
        		getFlags(DEFAULT_ADJUST_QUERY_PARAMETERS.getAdjustDegreeFlags()));
    	
    	adjustDegreeModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				model.setEnabled(adjustDegreeModel.getBooleanValue());
			}
		});
    	model.setEnabled(adjustDegreeModel.getBooleanValue());
    	
    	return model;
    }
    
    /**
     * Creates the settings model to be used to specify, how ring counts shall be adjusted,
     * which has only an effect, if the adjust ring counts option is enabled.
     * 
     * @param adjustRingCountModel Model that the model to be created depends on.
     * 	Based on the state of the passed in model we will enable or disable the
     *  AdjustDegreeFlags model.
     * 
     * @return Settings model for defining ring count adjustment.
     */
    static final SettingsModelEnumerationArray<AdjustQueryWhichFlags> createAdjustRingCountFlagsOptionModel(
    		final SettingsModelBoolean adjustRingCountModel) {
    	SettingsModelEnumerationArray<AdjustQueryWhichFlags> model =
    			new SettingsModelEnumerationArray<>(AdjustQueryWhichFlags.class, "adjust_ring_count_flags", 
    					getFlags(DEFAULT_ADJUST_QUERY_PARAMETERS.getAdjustRingCountFlags()));

    	adjustRingCountModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				model.setEnabled(adjustRingCountModel.getBooleanValue());
			}
		});
    	
    	model.setEnabled(adjustRingCountModel.getBooleanValue());
    	
    	return model;    
    }   

    /**
     * Converts the passed in array of flags into a long value.
     * 
     * @param arrFlags Flags to be converted. Can be null to return 0.
     * 
     * @return Long value representation of flags.
     */
    static final long getFlags(AdjustQueryWhichFlags[] arrFlags) {
    	long lCombinedFlags = 0;
    	
    	if (arrFlags != null) {
    		for (AdjustQueryWhichFlags flag : arrFlags) {
    			lCombinedFlags += flag.swigValue();
    		}
    	}

    	return lCombinedFlags;
    }
    

    /**
     * Converts the passed long value into an array of flags.
     * 
     * @param lCombinedFlags Combined long value of flags to be converted.
     * 
     * @return Array of represented flags. Empty, if 0 was passed in or no flags were found.
     */
    static final AdjustQueryWhichFlags[] getFlags(long lCombinedFlags) {
    	List<AdjustQueryWhichFlags> listFlags = new ArrayList<>();
    	for (AdjustQueryWhichFlags flag : AdjustQueryWhichFlags.values()) {
    		// Check, if the bits are set in the combined long value
    		if ((flag.swigValue() & lCombinedFlags) == flag.swigValue()) {
    			listFlags.add(flag);
    		}
    	}
    	return listFlags.toArray(new AdjustQueryWhichFlags[listFlags.size()]);
    }
}
