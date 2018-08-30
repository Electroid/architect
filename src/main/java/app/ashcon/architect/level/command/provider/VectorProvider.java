package app.ashcon.architect.level.command.provider;

import app.ashcon.intake.argument.ArgumentException;
import app.ashcon.intake.argument.CommandArgs;
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider;
import app.ashcon.intake.parametric.ProvisionException;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.List;

public class VectorProvider implements BukkitProvider<Vector> {

    @Override
    public String getName() {
        return "vector";
    }

    @Nullable
    @Override
    public Vector get(CommandSender sender, CommandArgs args, List<? extends Annotation> mods) throws ArgumentException, ProvisionException {
        String[] components = args.next().split(",");
        if(components.length == 3) {
            double[] componentsDouble = new double[3];
            for(int i = 0; i < 3; i++) {
                try {
                    componentsDouble[i] = Double.parseDouble(components[i]);
                } catch(NumberFormatException nfe) {
                    throw new ArgumentException("Expected a number, got '" + components[i] + "'");
                }
            }
            return new Vector(componentsDouble[0], componentsDouble[1], componentsDouble[2]);
        } else {
            throw new ArgumentException("Vector requires 3 components 'x,y,z'");
        }
    }

}
