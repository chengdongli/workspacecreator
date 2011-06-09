package cli.common.ui;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class ColorManager {
    public static final ColorManager INSTANCE=new ColorManager();
    
    private ColorManager() {
    }

    public Color getColor(RGB rgb) {
        Assert.isNotNull(rgb);
        // The RAP version will be an SessionSingletonBase
        ColorRegistry registry = JFaceResources.getColorRegistry();
        ColorDescriptor colorDesc = registry.getColorDescriptor(rgb.toString());
        if (colorDesc == null) {
            registry.put(rgb.toString(), rgb);
        }
        return registry.get(rgb.toString());
    }
}
