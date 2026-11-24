# Steal the Moon

A Tamagotchi-style virtual pet simulator built with **Java and Swing**. You play Gru from *Despicable Me*: keep your Minions happy, earn coins, and save up enough to steal the moon.

Group project (Software Development and Problem Solving) by **Olsa** and **Lea**.

## How to play

1. You start with **one Minion** and **20 coins**.
2. A bubble pops up over a Minion's head showing what it needs: *Hungry*, *Sleepy* or *Play*. Click the matching button to fix it and earn **5 coins**. A wrong button shows a hint.
3. Spend coins to **add more Minions** (the price goes up each time) or to **hire Dr. Nefario** (50 coins), who walks around and helps Minions automatically for **15 coins** per task.
4. Reach **250 coins** and click **STEAL THE MOON** to trigger the victory animation.

## How it is built

- **Object-oriented design:** the main `MinionMayhemGame` window class (extends `JFrame`) plus inner classes `Minion` and `DrNefario`.
- **Layered UI:** a `JLayeredPane` stacks the background, characters and pop-up bubbles so images never hide behind the background.
- **Two timers:** a fast 50 ms game loop (moves Dr. Nefario, assigns targets) and a slow 3 s logic loop (decides when Minions get needs), both dispatched on the Swing Event Dispatch Thread.
- **Safe image loading:** a `loadIcon` helper draws a red placeholder if an image file is missing instead of crashing.
- **End sequence:** buttons are disabled in a loop while the "steal the moon" animation plays.

## Run it

Requires JDK 11+.

```
cd src
javac MinionMayhemGame.java
java MinionMayhemGame
```

The game loads these images from the same folder as the class file: `lab.jpg`, `minion.png`, `gru.png`, `happygru.jpg`, `meallti.jpg`, `succes.jpg`, `dr.jpg`. They are not included in this repository yet, so missing images are replaced by red squares.

## Contributions

- **Olsa:** visual design and user interface; finalized the codebase and made sure the software ran smoothly.
- **Lea:** original idea and core mechanics; project documentation; support during coding and implementation.

## Future work

Move to a Java game framework to handle more complex game physics and keep larger projects maintainable.
