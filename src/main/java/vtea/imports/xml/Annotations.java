/*
 * Copyright (C) 2021 SciJava
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package vtea.imports.xml;

import java.util.List;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 *
 * @author Seth
 */
@Root
public class Annotations {

    @ElementList(entry = "Annotation",inline = true)
    private List<Annotation> list;

    public List getAnnotations() {
        return list;
    }

    static class Annotation {

        @Attribute
        private String Visible;

        @Attribute
        private String Name;

        @Attribute
        private String LineColor;

        @Element
        protected Regions Regions;

        public String getName() {
            return Name;
        }

        public String getLineColor() {
            return LineColor;
        }

        public Regions getRegions() {
            return Regions;
        }

        static class Regions {

            @ElementList(entry = "Region",inline = true)
            private List<Region> list;

            public List getRegions() {
                return list;
            }

            static class Region {

                @Attribute
                private String NegativeROA;

                @Attribute
                private String HasEndcaps;

                @Attribute
                private String Type;

                //
                @Element
                protected Vertices Vertices;

                public String getType() {
                    return Type;
                }

                public Vertices getVertices() {
                    return Vertices;
                }

                static class Vertices {

                    @ElementList(entry = "V", inline = true)
                    private List<V> list;

                    public List getVertices() {
                        return list;
                    }

                    static class V {

                        @Attribute
                        private String Y;

                        @Attribute
                        private String X;

                        public String getY() {
                            return Y;
                        }

                        public String getX() {
                            return X;
                        }
                    }
                }
            }
        }
    }
}
