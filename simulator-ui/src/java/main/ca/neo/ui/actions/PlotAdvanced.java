package ca.neo.ui.actions;

import ca.neo.plot.Plotter;
import ca.neo.ui.configurable.ConfigException;
import ca.neo.ui.configurable.PropertyDescriptor;
import ca.neo.ui.configurable.PropertySet;
import ca.neo.ui.configurable.descriptors.PFloat;
import ca.neo.ui.configurable.descriptors.PInt;
import ca.neo.ui.configurable.managers.UserConfigurer;
import ca.neo.ui.configurable.managers.ConfigManager.ConfigMode;
import ca.neo.util.DataUtils;
import ca.neo.util.TimeSeries;
import ca.shu.ui.lib.actions.ActionException;
import ca.shu.ui.lib.actions.StandardAction;
import ca.shu.ui.lib.util.UIEnvironment;
import ca.shu.ui.lib.util.UserMessages;

/**
 * Action for Plotting with additional options
 * 
 * @author Shu Wu
 */
public class PlotAdvanced extends StandardAction {

	private static final long serialVersionUID = 1L;
	private TimeSeries timeSeries;
	private String plotName;

	public PlotAdvanced(TimeSeries timeSeries, String plotName) {
		super("Plot with options", "Plot w/ options");
		this.timeSeries = timeSeries;
		this.plotName = plotName;
	}

	@Override
	protected void action() throws ActionException {
		try {
			// float tauFilter = new Float(JOptionPane
			// .showInputDialog("Time constant of display filter (s): "));

			PFloat pTauFilter = new PFloat(
					"Time constant of display filter [0 = off] ");
			PInt pSubSampling = new PInt("Subsampling [0 = off]");

			PropertySet result;
			try {
				result = UserConfigurer.configure(new PropertyDescriptor[] {
						pTauFilter, pSubSampling }, "Plot Options",
						UIEnvironment.getInstance(),
						ConfigMode.TEMPLATE_NOT_CHOOSABLE);

				float tauFilter = (Float) result.getProperty(pTauFilter);
				int subSampling = (Integer) result.getProperty(pSubSampling);

				TimeSeries timeSeriesToShow;

				if (subSampling != 0) {
					timeSeriesToShow = DataUtils.subsample(timeSeries,
							subSampling);
				} else {
					timeSeriesToShow = timeSeries;
				}

				if (tauFilter != 0) {
					Plotter.plot(timeSeriesToShow, tauFilter, plotName);
				} else {
					Plotter.plot(timeSeriesToShow, plotName);
				}
			} catch (ConfigException e) {
				e.defaultHandleBehavior();
			}

		} catch (java.lang.NumberFormatException exception) {
			UserMessages.showWarning("Could not parse number");
		}

	}
}