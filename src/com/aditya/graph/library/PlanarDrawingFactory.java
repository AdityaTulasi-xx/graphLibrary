package com.aditya.graph.library;

public class PlanarDrawingFactory
{
    public static IPlanarDrawingMethods GetPlanarDrawingStrategy(PlanarDrawingStrategies strategy)
    {
        switch (strategy)
        {
            case SCHNYDER:
                return new ShiftPlanarDrawingStrategy();
            default:
                return new ShiftPlanarDrawingStrategy();
        }
    }
}
