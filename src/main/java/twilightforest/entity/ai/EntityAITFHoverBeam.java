package twilightforest.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import twilightforest.entity.boss.EntityTFSnowQueen;
import twilightforest.entity.boss.EntityTFSnowQueen.Phase;

import java.util.List;

public class EntityAITFHoverBeam extends EntityAITFHoverBase<EntityTFSnowQueen> {

	private int hoverTimer;
	private int beamTimer;
	private int seekTimer;

	private final int maxHoverTime;
	private final int maxBeamTime;
	private final int maxSeekTime;

	private double beamY;
	private boolean isInPosition;

	public EntityAITFHoverBeam(EntityTFSnowQueen snowQueen, Class<EntityPlayer> targetClass, int hoverTime, int dropTime) {
		super(snowQueen, targetClass, 3F, 4F);

		this.setMutexBits(3);
		this.maxHoverTime = hoverTime;
		this.maxSeekTime = hoverTime;
		this.maxBeamTime = dropTime;

		this.hoverTimer = 0;
		this.isInPosition = false;
	}

	@Override
	public boolean shouldExecute() {
		EntityLivingBase target = this.attacker.getAttackTarget();

		if (target == null) {
			return false;
		} else if (!target.isEntityAlive()) {
			return false;
		} else if (this.classTarget != null && !this.classTarget.isAssignableFrom(target.getClass())) {
			return false;
		} else if (this.attacker.getCurrentPhase() != Phase.BEAM) {
			return false;
		} else {
			return true;//attacker.canEntityBeSeen(target);
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		EntityLivingBase target = this.attacker.getAttackTarget();

		if (target == null || !target.isEntityAlive()) {
			return false;
		} else if (this.attacker.getCurrentPhase() != Phase.BEAM) {
			return false;
		} else if (this.seekTimer >= this.maxSeekTime) {
			return false;
		} else if (this.beamTimer >= this.maxBeamTime) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void resetTask() {
		this.seekTimer = 0;
		this.hoverTimer = 0;
		this.beamTimer = 0;
		this.isInPosition = false;

		this.attacker.setBreathing(false);
	}

	@Override
	public void updateTask() {

		// check if we're in position
		if (this.attacker.getDistanceSq(hoverPosX, hoverPosY, hoverPosZ) <= 1.0F) {
			this.isInPosition = true;
		}

		// update timers
		if (this.isInPosition) {
			this.hoverTimer++;
		} else {
			this.seekTimer++;
		}

		// drop once the hover timer runs out
		if (this.hoverTimer >= this.maxHoverTime) {
			this.beamTimer++;
			this.attacker.setBreathing(true);

			// anyhoo, deal damage
			doRayAttack();

			// descend
			this.hoverPosY -= 0.05F;

			if (this.hoverPosY < this.beamY) {
				this.hoverPosY = this.beamY;
			}
		}

		// check if we are at our waypoint target
		double offsetX = this.hoverPosX - this.attacker.posX;
		double offsetY = this.hoverPosY - this.attacker.posY;
		double offsetZ = this.hoverPosZ - this.attacker.posZ;

		double distanceDesired = offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ;

		distanceDesired = (double) MathHelper.sqrt(distanceDesired);

		if (distanceDesired > 0.5) {

			// add velocity
			double velX = offsetX / distanceDesired * 0.05D;
			double velY = offsetY / distanceDesired * 0.1D;
			double velZ = offsetZ / distanceDesired * 0.05D;

			// gravity offset
			velY += 0.02F;

			this.attacker.addVelocity(velX, velY, velZ);
		}

		// look at target
		EntityLivingBase target = this.attacker.getAttackTarget();
		if (target != null) {
			float tracking = this.isInPosition ? 1F : 20.0F;

			this.attacker.faceEntity(target, tracking, tracking);
			this.attacker.getLookHelper().setLookPositionWithEntity(target, tracking, tracking);
		}
	}

	private void doRayAttack() {

		double range = 20.0D;
		double offset = 10.0D;
		Vec3d srcVec = new Vec3d(this.attacker.posX, this.attacker.posY + 0.25, this.attacker.posZ);
		Vec3d lookVec = this.attacker.getLook(1.0F);
		Vec3d destVec = srcVec.add(lookVec.x * range, lookVec.y * range, lookVec.z * range);
		List<Entity> possibleList = this.attacker.world.getEntitiesWithinAABBExcludingEntity(this.attacker, this.attacker.getEntityBoundingBox().offset(lookVec.x * offset, lookVec.y * offset, lookVec.z * offset).grow(range, range, range));
		double hitDist = 0;

		for (Entity possibleEntity : possibleList) {
			if (possibleEntity.canBeCollidedWith() && possibleEntity != this.attacker) {
				float borderSize = possibleEntity.getCollisionBorderSize();
				AxisAlignedBB collisionBB = possibleEntity.getEntityBoundingBox().grow((double) borderSize, (double) borderSize, (double) borderSize);
				RayTraceResult interceptPos = collisionBB.calculateIntercept(srcVec, destVec);

				if (collisionBB.contains(srcVec)) {
					if (0.0D < hitDist || hitDist == 0.0D) {
						attacker.doBreathAttack(possibleEntity);
						hitDist = 0.0D;
					}
				} else if (interceptPos != null) {
					double possibleDist = srcVec.distanceTo(interceptPos.hitVec);

					if (possibleDist < hitDist || hitDist == 0.0D) {
						attacker.doBreathAttack(possibleEntity);
						hitDist = possibleDist;
					}
				}
			}
		}
	}

	@Override
	protected void makeNewHoverSpot(EntityLivingBase target) {
		super.makeNewHoverSpot(target);
		this.beamY = target.posY;
		this.seekTimer = 0;
	}
}
