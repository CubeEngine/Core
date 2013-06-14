/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.CubeEngine;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import static de.cubeisland.cubeengine.core.logger.LogLevel.WARNING;

/**
 * This is a global ClassLoader that should be used to load classes from libraries.
 */
public class LibraryClassLoader extends URLClassLoader
{
    public LibraryClassLoader(ClassLoader parent)
    {
        super(new URL[0], parent);
    }

    @Override
    public void addURL(URL url)
    {
        super.addURL(url);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException
    {
        return super.findClass(name);
    }

    public void shutdown()
    {
        try
        {
            Method method = this.getClass().getMethod("close");
            method.setAccessible(true);
            method.invoke(this);
        }
        catch (Exception ignored)
        {
            CubeEngine.getLog().warn("Failed to close the library class loader");
        }
    }
}
