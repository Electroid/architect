package app.ashcon.architect.level.command.provider;

import app.ashcon.intake.argument.ArgumentException;
import app.ashcon.intake.argument.CommandArgs;
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider;
import app.ashcon.intake.parametric.ProvisionException;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VectorProvider implements BukkitProvider<Vector> {

    @Override
    public String getName() {
        return "vector";
    }

    @Override
    public Vector get(CommandSender sender, CommandArgs args, List<? extends Annotation> mods) throws ArgumentException, ProvisionException {
        final String query = args.next();
        try {
            final List<Double> components = Stream.of(args.next().split(",")).map(Double::parseDouble).collect(Collectors.toList());
            if(components.size() != 3) {
                throw new NumberFormatException("Vector must be three components");
            }
            return new Vector(components.get(0), components.get(1), components.get(2));
        } catch(NumberFormatException error) {
            throw new ArgumentException("Expected a (x,y,z) vector, got '" + query + "'", error);
        }
    }

}
