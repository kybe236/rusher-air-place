package org.kybe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.events.client.input.EventMouse;
import org.rusherhack.client.api.events.render.EventRender2D;
import org.rusherhack.client.api.events.render.EventRender3D;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.render.IRenderer2D;
import org.rusherhack.client.api.render.IRenderer3D;
import org.rusherhack.client.api.render.font.IFontRenderer;
import org.rusherhack.client.api.setting.BindSetting;
import org.rusherhack.client.api.setting.ColorSetting;
import org.rusherhack.client.api.utils.ChatUtils;
import org.rusherhack.client.api.utils.WorldUtils;
import org.rusherhack.core.bind.key.NullKey;
import org.rusherhack.core.event.stage.Stage;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;
import org.rusherhack.core.setting.StringSetting;
import org.rusherhack.core.utils.ColorUtils;

import javax.annotation.processing.SupportedSourceVersion;
import java.awt.*;

/**
 * Grim AirPlace
 *
 * @author kybe236
 */
public class GrimAirPlace extends ToggleableModule {
	int cooldown = 0;
	BlockHitResult hit;
	BlockPos pos = null;

	private final NumberSetting<Integer> range = new NumberSetting<>("Range", "Range for AirPlace", 5, 0, 7);
	private final BooleanSetting swing = new BooleanSetting("Swing", "Swing hand", true);
	private final NumberSetting<Integer> delay = new NumberSetting<>("Delay", "Delay for AirPlace", 4, 0, 10);
	private final BooleanSetting grim = new BooleanSetting("Grim", "Grim", false);
	private final ColorSetting color = new ColorSetting("Color", "Color", new Color(0, 0, 0, 0).getRGB());


	public GrimAirPlace() {
		super("Grim Air Place", "Grim Air Place", ModuleCategory.MISC);

		this.registerSettings(
				range,
				swing,
				delay,
				color,
				grim
		);
	}

	@Subscribe(stage = Stage.PRE)
	public void onPreTick(EventUpdate e) {
		if (mc.player == null || mc.getCameraEntity() == null || mc.gameMode == null) return;
		// Dont decrease cooldown in the pre function
		if (cooldown > 0) return;

		HitResult thit = mc.getCameraEntity().pick(range.getValue(), 0, false);
		if (!(thit instanceof BlockHitResult bhr) || !mc.level.getBlockState(bhr.getBlockPos()).getBlock().equals(Blocks.AIR)) return;
		pos = bhr.getBlockPos();
	}

	@Subscribe(stage = Stage.POST)
	public void onTick(EventUpdate e) {
		if (mc.player == null || mc.getCameraEntity() == null || mc.gameMode == null) return;
		if (cooldown > 0) {
			cooldown--;
			return;
		}
		HitResult thit = mc.getCameraEntity().pick(range.getValue(), 0, false);

		if (!(thit instanceof BlockHitResult bhr) || pos == null || !pos.equals(bhr.getBlockPos()) || !mc.level.getBlockState(bhr.getBlockPos()).getBlock().equals(Blocks.AIR)) return;
		hit = bhr;

		boolean main = mc.player.getMainHandItem().getItem() instanceof BlockItem;
		boolean off = mc.player.getOffhandItem().getItem() instanceof BlockItem;

		if (mc.options.keyUse.isDown() && (main || off) && cooldown <= 0) {
			if (grim.getValue()) {
				mc.player.connection.send(
						new ServerboundPlayerActionPacket(
								ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND,
								BlockPos.ZERO,
								Direction.UP
						)
				);
			}

			InteractionHand hand = main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			if (grim.getValue()) {
				hand = (hand == InteractionHand.MAIN_HAND) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
			}

			mc.gameMode.useItemOn(
					mc.player,
					hand,
					hit
			);
			if (swing.getValue())
				mc.player.swing(main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
			else
				mc.player.connection.send(
						new ServerboundSwingPacket(
								hand
						)
				);

			if (grim.getValue()) {
				mc.player.connection.send(
						new ServerboundPlayerActionPacket(
								ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND,
								BlockPos.ZERO,
								Direction.UP
						)
				);
			}

			cooldown = delay.getValue();
		}
	}

	@Subscribe
	public void onRender3D(EventRender3D e) {
		if (hit == null || mc.level == null || !mc.level.getBlockState(hit.getBlockPos()).getBlock().equals(Blocks.AIR) || (!(mc.player.getMainHandItem().getItem() instanceof BlockItem) && !(mc.player.getOffhandItem().getItem() instanceof BlockItem)))
			return;
		final IRenderer3D renderer = e.getRenderer();

		final int fcolor = ColorUtils.transparency(this.color.getValueRGB(), 0.5f);

		renderer.begin(e.getMatrixStack());

		renderer.drawBox(
				pos,
				true,
				true,
				fcolor
		);

		renderer.end();
	}
}
