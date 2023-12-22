"use strict";

import { Response, Request, NextFunction } from "express";
import { Stream, StreamDocument } from "../../models/Stream";


/**
 * List of API examples.
 * @route GET /api
 */
export const getStreams = (req: Request, res: Response) => {
    Stream.find((error, streamDocuments) => {
        console.log("Got documents:", streamDocuments.length);
        streamDocuments.forEach(document => console.log(document));
    });
};

export const readyToStream = async (req: Request, res: Response) => {
    const body = req.body;
    try {
        res.writeHead(200, {
            'Content-Type': 'text/event-stream',
            'Cache-Control': 'no-cache',
            'Connection': 'keep-alive'
        });
        let count = 0
        const i = setInterval(() => {
            res.write('data: hello!\n\n')
            if (++count > 60) {
                clearInterval(i);
                res.end();
            }
        }, 1000);
    } catch (error) {
        console.error("error:", error);
        res.json({
            error: error.message
        }).send(500);
    }
};