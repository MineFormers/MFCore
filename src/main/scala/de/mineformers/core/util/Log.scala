/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 MineFormers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.mineformers.core.util

import org.apache.logging.log4j.{Marker, Level, LogManager}
import org.apache.logging.log4j.message.Message

/**
 * Log
 *
 * @author PaleoCrafter
 */
object Log {
  def catching(level: Level, t: Throwable): Unit = underlying.catching(level, t)

  def catching(t: Throwable): Unit = underlying.catching(t)

  def debug(marker: Marker, msg: Message): Unit = underlying.debug(marker, msg)

  def debug(marker: Marker, msg: Message, t: Throwable): Unit = underlying.debug(marker, msg, t)

  def debug(marker: Marker, message: scala.Any): Unit = underlying.debug(marker, message)

  def debug(marker: Marker, message: scala.Any, t: Throwable): Unit = underlying.debug(marker, message, t)

  def debug(marker: Marker, message: String): Unit = underlying.debug(marker, message)

  def debug(marker: Marker, message: String, params: AnyRef*): Unit = underlying.debug(marker, message, params)

  def debug(marker: Marker, message: String, t: Throwable): Unit = underlying.debug(marker, message, t)

  def debug(msg: Message): Unit = underlying.debug(msg)

  def debug(msg: Message, t: Throwable): Unit = underlying.debug(msg, t)

  def debug(message: scala.Any): Unit = underlying.debug(message)

  def debug(message: scala.Any, t: Throwable): Unit = underlying.debug(message, t)

  def debug(message: String): Unit = underlying.debug(message)

  def debug(message: String, params: AnyRef*): Unit = underlying.debug(message, params)

  def debug(message: String, t: Throwable): Unit = underlying.debug(message, t)

  def entry(): Unit = underlying.entry()

  def entry(params: AnyRef*): Unit = underlying.entry(params)

  def error(marker: Marker, msg: Message): Unit = underlying.error(marker, msg)

  def error(marker: Marker, msg: Message, t: Throwable): Unit = underlying.error(marker, msg, t)

  def error(marker: Marker, message: scala.Any): Unit = underlying.error(marker, message)

  def error(marker: Marker, message: scala.Any, t: Throwable): Unit = underlying.error(marker, message, t)

  def error(marker: Marker, message: String): Unit = underlying.error(marker, message)

  def error(marker: Marker, message: String, params: AnyRef*): Unit = underlying.error(marker, message, params)

  def error(marker: Marker, message: String, t: Throwable): Unit = underlying.error(marker, message, t)

  def error(msg: Message): Unit = underlying.error(msg)

  def error(msg: Message, t: Throwable): Unit = underlying.error(msg, t)

  def error(message: scala.Any): Unit = underlying.error(message)

  def error(message: scala.Any, t: Throwable): Unit = underlying.error(message, t)

  def error(message: String): Unit = underlying.error(message)

  def error(message: String, params: AnyRef*): Unit = underlying.error(message, params)

  def error(message: String, t: Throwable): Unit = underlying.error(message, t)

  def fatal(marker: Marker, msg: Message): Unit = underlying.fatal(marker, msg)

  def fatal(marker: Marker, msg: Message, t: Throwable): Unit = underlying.fatal(marker, msg, t)

  def fatal(marker: Marker, message: scala.Any): Unit = underlying.fatal(marker, message)

  def fatal(marker: Marker, message: scala.Any, t: Throwable): Unit = underlying.fatal(marker, message, t)

  def fatal(marker: Marker, message: String): Unit = underlying.fatal(marker, message)

  def fatal(marker: Marker, message: String, params: AnyRef*): Unit = underlying.fatal(marker, message, params)

  def fatal(marker: Marker, message: String, t: Throwable): Unit = underlying.fatal(marker, message, t)

  def fatal(msg: Message): Unit = underlying.fatal(msg)

  def fatal(msg: Message, t: Throwable): Unit = underlying.fatal(msg, t)

  def fatal(message: scala.Any): Unit = underlying.fatal(message)

  def fatal(message: scala.Any, t: Throwable): Unit = underlying.fatal(message, t)

  def fatal(message: String): Unit = underlying.fatal(message)

  def fatal(message: String, params: AnyRef*): Unit = underlying.fatal(message, params)

  def fatal(message: String, t: Throwable): Unit = underlying.fatal(message, t)

  def info(marker: Marker, msg: Message): Unit = underlying.info(marker, msg)

  def info(marker: Marker, msg: Message, t: Throwable): Unit = underlying.info(marker, msg, t)

  def info(marker: Marker, message: scala.Any): Unit = underlying.info(marker, message)

  def info(marker: Marker, message: scala.Any, t: Throwable): Unit = underlying.info(marker, message, t)

  def info(marker: Marker, message: String): Unit = underlying.info(marker, message)

  def info(marker: Marker, message: String, params: AnyRef*): Unit = underlying.info(marker, message, params)

  def info(marker: Marker, message: String, t: Throwable): Unit = underlying.info(marker, message, t)

  def info(msg: Message): Unit = underlying.info(msg)

  def info(msg: Message, t: Throwable): Unit = underlying.info(msg, t)

  def info(message: scala.Any): Unit = underlying.info(message)

  def info(message: scala.Any, t: Throwable): Unit = underlying.info(message, t)

  def info(message: String): Unit = underlying.info(message)

  def info(message: String, params: AnyRef*): Unit = underlying.info(message, params)

  def info(message: String, t: Throwable): Unit = underlying.info(message, t)

  def log(level: Level, marker: Marker, msg: Message): Unit = underlying.log(level, marker, msg)

  def log(level: Level, marker: Marker, msg: Message, t: Throwable): Unit = underlying.log(level, marker, msg, t)

  def log(level: Level, marker: Marker, message: scala.Any): Unit = underlying.log(level, marker, message)

  def log(level: Level, marker: Marker, message: scala.Any, t: Throwable): Unit = underlying.log(level, marker, message, t)

  def log(level: Level, marker: Marker, message: String): Unit = underlying.log(level, marker, message)

  def log(level: Level, marker: Marker, message: String, params: AnyRef*): Unit = underlying.log(level, marker, message, params)

  def log(level: Level, marker: Marker, message: String, t: Throwable): Unit = underlying.log(level, marker, message, t)

  def log(level: Level, msg: Message): Unit = underlying.log(level, msg)

  def log(level: Level, msg: Message, t: Throwable): Unit = underlying.log(level, msg, t)

  def log(level: Level, message: scala.Any): Unit = underlying.log(level, message)

  def log(level: Level, message: scala.Any, t: Throwable): Unit = underlying.log(level, message, t)

  def log(level: Level, message: String): Unit = underlying.log(level, message)

  def log(level: Level, message: String, params: AnyRef*): Unit = underlying.log(level, message, params)

  def log(level: Level, message: String, t: Throwable): Unit = underlying.log(level, message, t)

  def printf(level: Level, marker: Marker, format: String, params: AnyRef*): Unit = underlying.printf(level, marker, format, params)

  def printf(level: Level, format: String, params: AnyRef*): Unit = underlying.printf(level, format, params)

  def throwing[T <: Throwable](level: Level, t: T): T = underlying.throwing(level, t)

  def throwing[T <: Throwable](t: T): T = underlying.throwing(t)

  def trace(marker: Marker, msg: Message): Unit = underlying.trace(marker, msg)

  def trace(marker: Marker, msg: Message, t: Throwable): Unit = underlying.trace(marker, msg, t)

  def trace(marker: Marker, message: scala.Any): Unit = underlying.trace(marker, message)

  def trace(marker: Marker, message: scala.Any, t: Throwable): Unit = underlying.trace(marker, message, t)

  def trace(marker: Marker, message: String): Unit = underlying.trace(marker, message)

  def trace(marker: Marker, message: String, params: AnyRef*): Unit = underlying.trace(marker, message, params)

  def trace(marker: Marker, message: String, t: Throwable): Unit = underlying.trace(marker, message, t)

  def trace(msg: Message): Unit = underlying.trace(msg)

  def trace(msg: Message, t: Throwable): Unit = underlying.trace(msg, t)

  def trace(message: scala.Any): Unit = underlying.trace(message)

  def trace(message: scala.Any, t: Throwable): Unit = underlying.trace(message, t)

  def trace(message: String): Unit = underlying.trace(message)

  def trace(message: String, params: AnyRef*): Unit = underlying.trace(message, params)

  def trace(message: String, t: Throwable): Unit = underlying.trace(message, t)

  def warn(marker: Marker, msg: Message): Unit = underlying.warn(marker, msg)

  def warn(marker: Marker, msg: Message, t: Throwable): Unit = underlying.warn(marker, msg, t)

  def warn(marker: Marker, message: scala.Any): Unit = underlying.warn(marker, message)

  def warn(marker: Marker, message: scala.Any, t: Throwable): Unit = underlying.warn(marker, message, t)

  def warn(marker: Marker, message: String): Unit = underlying.warn(marker, message)

  def warn(marker: Marker, message: String, params: AnyRef*): Unit = underlying.warn(marker, message, params)

  def warn(marker: Marker, message: String, t: Throwable): Unit = underlying.warn(marker, message, t)

  def warn(msg: Message): Unit = underlying.warn(msg)

  def warn(msg: Message, t: Throwable): Unit = underlying.warn(msg, t)

  def warn(message: scala.Any): Unit = underlying.warn(message)

  def warn(message: scala.Any, t: Throwable): Unit = underlying.warn(message, t)

  def warn(message: String): Unit = underlying.warn(message)

  def warn(message: String, params: AnyRef*): Unit = underlying.warn(message, params)

  def warn(message: String, t: Throwable): Unit = underlying.warn(message, t)

  private val underlying = LogManager.getLogger("MFCore")
}