/*
 * Copyright (C) 2018-2026 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.notcoral.velocity.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.proxy.VelocityServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.notcoral.velocity.bandwidth.BvBandwidthStats;
import top.notcoral.velocity.config.BvConfiguration;

class BvCommandTest {

  private CommandDispatcher<CommandSource> dispatcher;
  private CommandSource source;

  @BeforeEach
  void setUp() {
    BvBandwidthStats.INSTANCE.reset();
    final VelocityServer server = mock(VelocityServer.class);
    final BvConfiguration configuration = mock(BvConfiguration.class);
    final BvConfiguration.Optimization optimization =
        mock(BvConfiguration.Optimization.class);
    when(server.getBvConfiguration()).thenReturn(configuration);
    when(configuration.getOptimization()).thenReturn(optimization);
    when(optimization.isPacketBandwidthStatsEnabled()).thenReturn(true);

    source = mock(CommandSource.class);
    when(source.getPermissionValue("bvelocity.command.bandwidth")).thenReturn(Tristate.TRUE);
    when(source.getPermissionValue("bvelocity.command.bandwidth.reset"))
        .thenReturn(Tristate.TRUE);

    dispatcher = new CommandDispatcher<>();
    dispatcher.getRoot().addChild(BvCommand.create(server).getNode());
  }

  @Test
  void acceptsBandwidthCommandVariants() throws CommandSyntaxException {
    assertEquals(1, dispatcher.execute("bvelocity bandwidth", source));
    assertEquals(1, dispatcher.execute("bvelocity bandwidth packets 5 2", source));
    assertEquals(1, dispatcher.execute("bvelocity bandwidth packets outbound 5 2", source));
    assertEquals(1, dispatcher.execute("bvelocity bandwidth players 5", source));
    assertEquals(1, dispatcher.execute("bvelocity bandwidth players inbound 5", source));
    assertEquals(1, dispatcher.execute("bvelocity bandwidth reset", source));
  }

  @Test
  void rejectsBandwidthTopValuesOutsideTheDocumentedRange() {
    assertThrows(CommandSyntaxException.class,
        () -> dispatcher.execute("bvelocity bandwidth packets 0", source));
    assertThrows(CommandSyntaxException.class,
        () -> dispatcher.execute("bvelocity bandwidth packets 51", source));
    assertThrows(CommandSyntaxException.class,
        () -> dispatcher.execute("bvelocity bandwidth players 0", source));
    assertThrows(CommandSyntaxException.class,
        () -> dispatcher.execute("bvelocity bandwidth players 51", source));
  }
}
