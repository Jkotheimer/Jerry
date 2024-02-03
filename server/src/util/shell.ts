import {
    ChildProcess,
    ExecException,
    exec,
    spawn as _spawn
} from 'child_process';

export interface ShellResult {
    childProcess: ChildProcess;
    error: ExecException;
    stdout: string;
    stderr: string;
}

export const call = async (cmd: string): Promise<ShellResult> => {
    return new Promise((resolve, reject) => {
        const childProcess = exec(
            cmd,
            (error: ExecException, stdout: string, stderr: string) => {
                const result = {
                    childProcess,
                    error,
                    stdout,
                    stderr
                } as ShellResult;

                if (error) {
                    return reject(result);
                }
                return resolve(result);
            }
        );
    });
};

export const spawn = (cmd: string) => {
    const cmdTokens = cmd.split(' ');
    if (!cmdTokens.length) {
        return null;
    }
    return _spawn(cmdTokens[0], cmdTokens.slice(1));
};
