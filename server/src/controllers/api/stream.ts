'use strict';

import { Response, Request, NextFunction } from 'express';
//import { Stream, StreamDocument } from '../../models/Stream';
import * as shell from '../../util/shell';
import logger from '../../util/logger';
import bcrypt from 'bcrypt-nodejs';

export interface Stream {
    timestamp: number;
    key: string;
}

const streams: Record<string, Stream> = {};

/**
 * List of API examples.
 * @route GET /api
 */
export const getStreams = (req: Request, res: Response) => {
    logger.debug('Get Streams');
    res.send(200);
};

export const initializeStream = (req: Request, res: Response) => {
    logger.debug('Initialize stream');
    let key = '';
    while (key.length < 1024) {
        key += Buffer.from(bcrypt.genSaltSync()).toString('hex');
    }

    streams[key] = {
        timestamp: Date.now(),
        key
    };

    logger.debug('Stream key: ' + key);
    res.status(201).json({ key });
};

export const consumeStream = async (req: Request, res: Response) => {
    try {
        const key: string = req.params.streamId;
        const ipAddress = req.body.ipAddress;
        logger.debug(`Key: ${key}`);
        logger.debug('Ip: ' + ipAddress);

        if (!ipAddress) {
            res.status(400).json({
                message: 'IP Address unavailable'
            });
        }

        if (!streams.hasOwnProperty(key)) {
            res.status(404).json({
                message: 'Stream does not exist. Please initialize a stream.'
            });
            return;
        }

        const childProcess = shell.spawn(
            `ffmpeg -protocol_whitelist rtp,udp -i rtp://${ipAddress} -f hls -hls_time 3 -c:v h264 -flags +cgop -g 30 -hls_list_size 60 /home/jack/Projects/Jerry/server/streams/stream.m3u8`
        );
        logger.debug('Started ffmpeg process: ' + childProcess.pid);

        childProcess.stdout.on('data', (data) =>
            console.log('STDOUT: ' + data)
        );
        childProcess.stderr.on('data', (data) =>
            console.log('STDERR: ' + data)
        );

        setTimeout(() => {
            logger.debug('Killing process: ' + childProcess.pid);
            childProcess.kill();
        }, 15000);

        res.status(200).json({
            message: 'Success!'
        });
    } catch (error) {
        console.error('error:', error);
        res.status(500).json({
            error: error.message
        });
    }
};
